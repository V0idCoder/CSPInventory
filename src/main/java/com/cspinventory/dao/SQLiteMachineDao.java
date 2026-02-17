package com.cspinventory.dao;

import com.cspinventory.model.Machine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteMachineDao implements MachineDao {

    private final DatabaseManager databaseManager;
    private static final DateTimeFormatter DB_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter UI_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter UI_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public SQLiteMachineDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public List<Machine> findAll() {
        String sql = """
                SELECT rowid AS Id, NomReseau, SerieNmb, Model, Utilisateur, Emplacement, Site, Lieu,
                       IPv4RJ45, IPv4Wifi, MACEthernet, MACWifi, VLAN,
                       Garantie, Maintenance, Statut, Note, PurchaseDate, DateMiseEnService, DateModif
                FROM Machines
                ORDER BY NomReseau COLLATE NOCASE
                """;
        List<Machine> machines = new ArrayList<>();

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                machines.add(map(rs));
            }
            return machines;
        } catch (SQLException e) {
            throw new RuntimeException("Cannot load machines", e);
        }
    }

    @Override
    public Optional<Machine> findById(long id) {
        String sql = """
                SELECT rowid AS Id, NomReseau, SerieNmb, Model, Utilisateur, Emplacement, Site, Lieu,
                       IPv4RJ45, IPv4Wifi, MACEthernet, MACWifi, VLAN,
                       Garantie, Maintenance, Statut, Note, PurchaseDate, DateMiseEnService, DateModif
                FROM Machines
                WHERE rowid = ?
                """;
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Cannot load machine with id " + id, e);
        }
    }

    @Override
    public Machine save(Machine machine) {
        String sql = """
                INSERT INTO Machines (
                  NomReseau, SerieNmb, Model, Utilisateur, Emplacement, Site, Lieu,
                  IPv4RJ45, IPv4Wifi, MACEthernet, MACWifi, VLAN, Garantie, Maintenance,
                  Statut, Note, PurchaseDate, DateMiseEnService, DateModif
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(machine, ps);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    machine.setId(keys.getLong(1));
                }
            }
            return machine;
        } catch (SQLException e) {
            throw new RuntimeException("Cannot create machine", e);
        }
    }

    @Override
    public Machine update(Machine machine) {
        String sql = """
                UPDATE Machines SET
                  NomReseau = ?, SerieNmb = ?, Model = ?, Utilisateur = ?, Emplacement = ?, Site = ?, Lieu = ?,
                  IPv4RJ45 = ?, IPv4Wifi = ?, MACEthernet = ?, MACWifi = ?, VLAN = ?, Garantie = ?, Maintenance = ?,
                  Statut = ?, Note = ?, PurchaseDate = ?, DateMiseEnService = ?, DateModif = ?
                WHERE rowid = ?
                """;

        try (Connection conn = databaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(machine, ps);
            ps.setLong(20, machine.getId());
            int updatedRows = ps.executeUpdate();
            if (updatedRows != 1) {
                throw new IllegalArgumentException("Machine introuvable pour id " + machine.getId());
            }
            return machine;
        } catch (SQLException e) {
            throw new RuntimeException("Cannot update machine", e);
        }
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM Machines WHERE rowid = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot delete machine", e);
        }
    }

    @Override
    public boolean existsNomReseau(String nomReseau, Long excludeId) {
        String sql = "SELECT COUNT(1) FROM Machines WHERE lower(NomReseau) = lower(?)"
                + (excludeId != null ? " AND rowid <> ?" : "");

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nomReseau);
            if (excludeId != null) {
                ps.setLong(2, excludeId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Cannot validate NomReseau uniqueness", e);
        }
    }

    private void bind(Machine machine, PreparedStatement ps) throws SQLException {
        ps.setString(1, machine.getNomReseau());
        ps.setString(2, machine.getSerieNmb());
        ps.setString(3, machine.getModel());
        ps.setString(4, machine.getUtilisateur());
        ps.setString(5, machine.getEmplacement());
        ps.setString(6, machine.getSite());
        ps.setString(7, machine.getLieu());
        ps.setString(8, machine.getIpv4RJ45());
        ps.setString(9, machine.getIpv4Wifi());
        ps.setString(10, machine.getMacEthernet());
        ps.setString(11, machine.getMacWifi());
        ps.setString(12, machine.getVlan());
        ps.setInt(13, machine.isGarantie() ? 1 : 0);
        ps.setInt(14, machine.getStatut() != null && machine.getStatut().equalsIgnoreCase("Maintenance") ? 1 : 0);
        ps.setString(15, machine.getStatut());
        ps.setString(16, machine.getNote());
        ps.setString(17, machine.getPurchaseDate() != null ? machine.getPurchaseDate().toString() : null);
        ps.setString(18, machine.getDateMiseEnService() != null ? machine.getDateMiseEnService().toString() : null);
        ps.setString(19, machine.getDateModif() != null ? machine.getDateModif().format(DB_DATE_TIME_FORMAT) : null);
    }

    private Machine map(ResultSet rs) throws SQLException {
        Machine machine = new Machine();
        machine.setId(rs.getLong("Id"));
        machine.setNomReseau(rs.getString("NomReseau"));
        machine.setSerieNmb(rs.getString("SerieNmb"));
        machine.setModel(rs.getString("Model"));
        machine.setUtilisateur(rs.getString("Utilisateur"));
        machine.setEmplacement(rs.getString("Emplacement"));
        machine.setSite(rs.getString("Site"));
        machine.setLieu(rs.getString("Lieu"));
        machine.setIpv4RJ45(rs.getString("IPv4RJ45"));
        machine.setIpv4Wifi(rs.getString("IPv4Wifi"));
        machine.setMacEthernet(rs.getString("MACEthernet"));
        machine.setMacWifi(rs.getString("MACWifi"));
        machine.setVlan(rs.getString("VLAN"));
        machine.setGarantie(rs.getInt("Garantie") == 1);
        machine.setStatut(resolveStatut(rs.getInt("Maintenance"), rs.getString("Statut")));
        machine.setNote(rs.getString("Note"));

        machine.setPurchaseDate(parseDate(rs.getString("PurchaseDate")));
        machine.setDateMiseEnService(parseDate(rs.getString("DateMiseEnService")));
        machine.setDateModif(parseDateModif(rs.getString("DateModif")));

        return machine;
    }

    private String resolveStatut(int maintenance, String statut) {
        if (statut != null && !statut.isBlank()) {
            return statut;
        }
        if (maintenance == 1) {
            return "Maintenance";
        }
        return "Ok";
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String value = raw.trim();
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(value).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDate.parse(value, UI_DATE_FORMAT);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(value, UI_DATE_TIME_FORMAT).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return Instant.parse(value).atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private LocalDateTime parseDateModif(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String value = raw.trim();

        if (value.chars().allMatch(Character::isDigit)) {
            try {
                long millis = Long.parseLong(value);
                return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime();
            } catch (NumberFormatException ignored) {
                // fall through to other parsers
            }
        }

        try {
            return Timestamp.valueOf(value).toLocalDateTime();
        } catch (IllegalArgumentException ignored) {
            // fall through to other parsers
        }

        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
            // fall through to final parser
        }
        try {
            return LocalDateTime.parse(value, UI_DATE_TIME_FORMAT);
        } catch (DateTimeParseException ignored) {
            // fall through to final parser
        }
        try {
            return LocalDate.parse(value, UI_DATE_FORMAT).atStartOfDay();
        } catch (DateTimeParseException ignored) {
            // fall through to final parser
        }

        try {
            return Instant.parse(value).atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }
}
