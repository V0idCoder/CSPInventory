package com.cspinventory.dao;

import com.cspinventory.util.AppPaths;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public final class Database {

    private final AppPaths appPaths;

    public Database(AppPaths appPaths) {
        this.appPaths = Objects.requireNonNull(appPaths, "appPaths must not be null");
    }

    public Connection connect() throws SQLException {
        try {
            appPaths.ensureDirectories();
        } catch (RuntimeException e) {
            throw new SQLException("Unable to prepare database directories", e);
        }

        Connection connection = DriverManager.getConnection(appPaths.getJdbcUrl());
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA journal_mode=WAL");
            statement.execute("PRAGMA synchronous=NORMAL");
        } catch (SQLException e) {
            try {
                connection.close();
            } catch (SQLException closeError) {
                e.addSuppressed(closeError);
            }
            throw e;
        }

        return connection;
    }
}
