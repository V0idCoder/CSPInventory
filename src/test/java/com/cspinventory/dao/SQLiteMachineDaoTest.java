package com.cspinventory.dao;

import com.cspinventory.model.Machine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SQLiteMachineDaoTest {

    @TempDir
    Path tempDir;

    @Test
    void saveFindUpdateDeleteFlow() {
        SQLiteMachineDao dao = newDao();
        Machine machine = sampleMachine("PC-001");

        Machine saved = dao.save(machine);
        assertNotNull(saved.getId());

        Machine loaded = dao.findById(saved.getId()).orElseThrow();
        assertEquals("PC-001", loaded.getNomReseau());
        assertEquals("Ok", loaded.getStatut());

        loaded.setUtilisateur("Bob");
        loaded.setStatut("Maintenance");
        dao.update(loaded);

        Machine updated = dao.findById(saved.getId()).orElseThrow();
        assertEquals("Bob", updated.getUtilisateur());
        assertEquals("Maintenance", updated.getStatut());

        List<Machine> all = dao.findAll();
        assertEquals(1, all.size());

        dao.delete(saved.getId());
        assertTrue(dao.findById(saved.getId()).isEmpty());
    }

    @Test
    void existsNomReseauIsCaseInsensitiveAndSupportsExcludeId() {
        SQLiteMachineDao dao = newDao();
        Machine machine = sampleMachine("PC-ABC");
        Machine saved = dao.save(machine);

        assertTrue(dao.existsNomReseau("pc-abc", null));
        assertFalse(dao.existsNomReseau("pc-abc", saved.getId()));
        assertFalse(dao.existsNomReseau("pc-zzz", null));
    }

    @Test
    void updateThrowsWhenMachineDoesNotExist() {
        SQLiteMachineDao dao = newDao();
        Machine missing = sampleMachine("PC-MISSING");
        missing.setId(9999L);

        assertThrows(IllegalArgumentException.class, () -> dao.update(missing));
    }

    private SQLiteMachineDao newDao() {
        Path dbPath = tempDir.resolve("test.db");
        DatabaseManager manager = new DatabaseManager(dbPath.toString());
        manager.initialize();
        return new SQLiteMachineDao(manager);
    }

    private Machine sampleMachine(String nomReseau) {
        Machine machine = new Machine();
        machine.setNomReseau(nomReseau);
        machine.setSite("Site A");
        machine.setLieu("Bureau");
        machine.setEmplacement("Site A / Bureau");
        machine.setModel("Model X");
        machine.setUtilisateur("Alice");
        machine.setPurchaseDate(LocalDate.of(2024, 1, 10));
        machine.setDateMiseEnService(LocalDate.of(2024, 1, 15));
        machine.setStatut("Ok");
        return machine;
    }
}
