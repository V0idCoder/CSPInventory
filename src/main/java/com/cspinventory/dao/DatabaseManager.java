package com.cspinventory.dao;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class DatabaseManager {

    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());

    private final Path dbPath;

    public DatabaseManager(String dbFileName) {
        this.dbPath = Path.of(dbFileName).toAbsolutePath();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

    public void initialize() {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA journal_mode=WAL");
            statement.execute("PRAGMA busy_timeout=5000");
            migrateLegacyTable(statement);
            createMachinesTable(statement);
            ensureColumnExists(statement, "Maintenance", "INTEGER NOT NULL DEFAULT 0");
            ensureColumnExists(statement, "DateModif", "TEXT");
            createIndexes(statement);
            LOGGER.info("SQLite initialized at " + dbPath);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to initialize SQLite database", e);
        }
    }

    private void migrateLegacyTable(Statement statement) throws SQLException {
        boolean hasMachine = tableExists(statement, "Machine");
        boolean hasMachines = tableExists(statement, "Machines");
        if (hasMachine && !hasMachines) {
            statement.execute("ALTER TABLE Machine RENAME TO Machines");
        }
    }

    private void createMachinesTable(Statement statement) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS Machines (
                    Id INTEGER PRIMARY KEY AUTOINCREMENT,
                    NomReseau TEXT NOT NULL UNIQUE,
                    SerieNmb TEXT,
                    Model TEXT,
                    Utilisateur TEXT,
                    Emplacement TEXT,
                    Site TEXT,
                    Lieu TEXT,
                    IPv4RJ45 TEXT,
                    IPv4Wifi TEXT,
                    MACEthernet TEXT,
                    MACWifi TEXT,
                    VLAN TEXT,
                    Garantie INTEGER NOT NULL DEFAULT 0,
                    Maintenance INTEGER NOT NULL DEFAULT 0,
                    Statut TEXT,
                    Note TEXT,
                    PurchaseDate TEXT,
                    DateMiseEnService TEXT,
                    DateModif TEXT
                )
                """;
        statement.execute(sql);
    }

    private void ensureColumnExists(Statement statement, String columnName, String ddlDefinition) throws SQLException {
        if (!columnExists(statement, "Machines", columnName)) {
            statement.execute("ALTER TABLE Machines ADD COLUMN " + columnName + " " + ddlDefinition);
        }
    }

    private void createIndexes(Statement statement) throws SQLException {
        statement.execute("CREATE INDEX IF NOT EXISTS idx_machines_nomreseau ON Machines (NomReseau)");
        statement.execute("CREATE INDEX IF NOT EXISTS idx_machines_site ON Machines (Site)");
        statement.execute("CREATE INDEX IF NOT EXISTS idx_machines_lieu ON Machines (Lieu)");
    }

    private boolean tableExists(Statement statement, String tableName) throws SQLException {
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND lower(name)=lower('" + tableName + "')";
        try (ResultSet rs = statement.executeQuery(sql)) {
            return rs.next();
        }
    }

    private boolean columnExists(Statement statement, String tableName, String columnName) throws SQLException {
        try (ResultSet rs = statement.executeQuery("PRAGMA table_info('" + tableName + "')")) {
            while (rs.next()) {
                String name = rs.getString("name");
                if (name != null && name.equalsIgnoreCase(columnName)) {
                    return true;
                }
            }
            return false;
        }
    }

    public Path getDbPath() {
        return dbPath;
    }
}
