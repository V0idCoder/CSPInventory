package com.cspinventory.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class AppPaths {

    private static final String APP_HOME_ENV = "CSPINVENTORY_HOME";
    private static final String DEFAULT_HOME_DIR_NAME = "CSPInventory";

    private final Path appHome;
    private final Path dataDir;
    private final Path modelsDir;
    private final Path backupsDir;
    private final Path databasePath;

    private AppPaths(Path appHome) {
        Path normalizedHome = Objects.requireNonNull(appHome, "appHome must not be null")
                .toAbsolutePath()
                .normalize();

        this.appHome = normalizedHome;
        this.dataDir = normalizedHome.resolve("data");
        this.modelsDir = normalizedHome.resolve("models");
        this.backupsDir = normalizedHome.resolve("backups");
        this.databasePath = this.dataDir.resolve("inventory.db").toAbsolutePath().normalize();
    }

    public static AppPaths resolveDefault() {
        String envHome = trimToNull(System.getenv(APP_HOME_ENV));

        Path baseHome;
        if (envHome != null) {
            baseHome = Path.of(envHome);
        } else {
            String userHome = trimToNull(System.getProperty("user.home"));
            if (userHome == null) {
                throw new IllegalStateException("System property 'user.home' is not defined");
            }
            baseHome = Path.of(userHome).resolve(DEFAULT_HOME_DIR_NAME);
        }

        AppPaths paths = new AppPaths(baseHome);
        paths.ensureDirectories();
        return paths;
    }

    public void ensureDirectories() {
        try {
            Files.createDirectories(appHome);
            Files.createDirectories(dataDir);
            Files.createDirectories(modelsDir);
            Files.createDirectories(backupsDir);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create application directories under: " + appHome, e);
        }
    }

    public Path getAppHome() {
        return appHome;
    }

    public Path getDataDir() {
        return dataDir;
    }

    public Path getModelsDir() {
        return modelsDir;
    }

    public Path getBackupsDir() {
        return backupsDir;
    }

    public Path getDatabasePath() {
        return databasePath;
    }

    public String getJdbcUrl() {
        return "jdbc:sqlite:" + databasePath;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
