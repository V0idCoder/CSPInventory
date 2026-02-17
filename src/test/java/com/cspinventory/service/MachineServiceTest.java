package com.cspinventory.service;

import com.cspinventory.dao.MachineDao;
import com.cspinventory.model.Machine;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MachineServiceTest {

    @Test
    void createTrimsNomReseauAndSetsDateModif() {
        InMemoryMachineDao dao = new InMemoryMachineDao();
        MachineService service = new MachineService(dao);

        Machine machine = new Machine();
        machine.setNomReseau("  PC-001  ");

        Machine created = service.create(machine);

        assertTrue(created.getId() != null && created.getId() > 0);
        assertTrue("PC-001".equals(created.getNomReseau()));
        assertNotNull(created.getDateModif());
    }

    @Test
    void createRejectsBlankNomReseau() {
        InMemoryMachineDao dao = new InMemoryMachineDao();
        MachineService service = new MachineService(dao);
        Machine machine = new Machine();
        machine.setNomReseau("   ");

        assertThrows(IllegalArgumentException.class, () -> service.create(machine));
    }

    @Test
    void updateRequiresId() {
        InMemoryMachineDao dao = new InMemoryMachineDao();
        MachineService service = new MachineService(dao);
        Machine machine = new Machine();
        machine.setNomReseau("PC-002");

        assertThrows(IllegalArgumentException.class, () -> service.update(machine));
    }

    @Test
    void updateRejectsDuplicateNomReseau() {
        InMemoryMachineDao dao = new InMemoryMachineDao();
        MachineService service = new MachineService(dao);

        Machine first = new Machine();
        first.setNomReseau("PC-001");
        service.create(first);

        Machine second = new Machine();
        second.setNomReseau("PC-002");
        service.create(second);

        second.setNomReseau("pc-001");
        assertThrows(IllegalArgumentException.class, () -> service.update(second));
    }

    @Test
    void matchesUsesMachineFieldsCaseInsensitive() {
        InMemoryMachineDao dao = new InMemoryMachineDao();
        MachineService service = new MachineService(dao);

        Machine machine = new Machine();
        machine.setNomReseau("POSTE-ABC");
        machine.setUtilisateur("Alice");
        machine.setIpv4RJ45("10.1.2.3");

        assertTrue(service.matches(machine, "alice"));
        assertTrue(service.matches(machine, "10.1"));
        assertFalse(service.matches(machine, "bob"));
    }

    private static class InMemoryMachineDao implements MachineDao {
        private final List<Machine> storage = new ArrayList<>();
        private long seq = 1L;

        @Override
        public List<Machine> findAll() {
            return List.copyOf(storage);
        }

        @Override
        public Optional<Machine> findById(long id) {
            return storage.stream().filter(m -> m.getId() != null && m.getId() == id).findFirst();
        }

        @Override
        public Machine save(Machine machine) {
            machine.setId(seq++);
            storage.add(machine);
            return machine;
        }

        @Override
        public Machine update(Machine machine) {
            return machine;
        }

        @Override
        public void delete(long id) {
            storage.removeIf(m -> m.getId() != null && m.getId() == id);
        }

        @Override
        public boolean existsNomReseau(String nomReseau, Long excludeId) {
            return storage.stream().anyMatch(m -> {
                if (m.getNomReseau() == null) {
                    return false;
                }
                if (excludeId != null && m.getId() != null && m.getId().equals(excludeId)) {
                    return false;
                }
                return m.getNomReseau().equalsIgnoreCase(nomReseau);
            });
        }
    }
}
