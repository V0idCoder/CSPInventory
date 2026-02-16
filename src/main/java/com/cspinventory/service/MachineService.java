package com.cspinventory.service;

import com.cspinventory.dao.MachineDao;
import com.cspinventory.model.Machine;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MachineService {

    private final MachineDao machineDao;

    public MachineService(MachineDao machineDao) {
        this.machineDao = machineDao;
    }

    public List<Machine> findAll() {
        return machineDao.findAll();
    }

    public Machine create(Machine machine) {
        validate(machine, null);
        machine.setDateModif(LocalDateTime.now());
        return machineDao.save(machine);
    }

    public Machine update(Machine machine) {
        if (machine.getId() == null) {
            throw new IllegalArgumentException("L'ID est obligatoire pour une mise a jour");
        }
        validate(machine, machine.getId());
        machine.setDateModif(LocalDateTime.now());
        return machineDao.update(machine);
    }

    public void delete(long id) {
        machineDao.delete(id);
    }

    public boolean isNomReseauTaken(String nomReseau, Long excludeId) {
        String value = Objects.toString(nomReseau, "").trim();
        if (value.isEmpty()) {
            return false;
        }
        return machineDao.existsNomReseau(value, excludeId);
    }

    public boolean matches(Machine machine, String searchText) {
        if (searchText == null || searchText.isBlank()) {
            return true;
        }

        String needle = searchText.toLowerCase(Locale.ROOT).trim();
        return contains(machine.getNomReseau(), needle)
                || contains(machine.getSerieNmb(), needle)
                || contains(machine.getModel(), needle)
                || contains(machine.getUtilisateur(), needle)
                || contains(machine.getEmplacement(), needle)
                || contains(machine.getSite(), needle)
                || contains(machine.getLieu(), needle)
                || contains(machine.getIpv4RJ45(), needle)
                || contains(machine.getIpv4Wifi(), needle)
                || contains(machine.getMacEthernet(), needle)
                || contains(machine.getMacWifi(), needle)
                || contains(machine.getVlan(), needle)
                || contains(machine.getStatut(), needle)
                || contains(machine.getNote(), needle)
                || contains(machine.getPurchaseDate(), needle)
                || contains(machine.getDateMiseEnService(), needle)
                || contains(machine.getDateModif(), needle)
                || contains(Boolean.toString(machine.isGarantie()), needle);
    }

    private void validate(Machine machine, Long excludeId) {
        if (machine == null) {
            throw new IllegalArgumentException("Machine invalide");
        }

        String nomReseau = Objects.toString(machine.getNomReseau(), "").trim();
        if (nomReseau.isEmpty()) {
            throw new IllegalArgumentException("NomReseau est obligatoire");
        }

        if (machineDao.existsNomReseau(nomReseau, excludeId)) {
            throw new IllegalArgumentException("NomReseau existe deja");
        }

        machine.setNomReseau(nomReseau);
    }

    private boolean contains(Object value, String needle) {
        return value != null && value.toString().toLowerCase(Locale.ROOT).contains(needle);
    }
}
