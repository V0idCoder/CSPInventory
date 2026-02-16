package com.cspinventory.dao;

import com.cspinventory.model.Machine;

import java.util.List;
import java.util.Optional;

public interface MachineDao {
    List<Machine> findAll();

    Optional<Machine> findById(long id);

    Machine save(Machine machine);

    Machine update(Machine machine);

    void delete(long id);

    boolean existsNomReseau(String nomReseau, Long excludeId);
}
