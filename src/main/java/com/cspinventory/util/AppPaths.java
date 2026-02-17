package com.cspinventory.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AppPaths {

    private static final String APP_HOME_PROPERTY = "cspinventory.home";
    private static final String APP_HOME_ENV = "CSPINVENTORY_HOME";

    private final Path appHome;
    private final Path dataDir;
    private final Path modelsDir;
    private final Path backupsDir;

    private AppPaths(Path appHome) {
        this.appHome = appHome.toAbsolutePath().normalize();
        this.dataDir = this.appHome.resolve("data");
        this.modelsDir = this.appHome.resolve("models");
        this.backupsDir = this.appHome.resolve("backups");
    }

    public static AppPaths resolveDefault() {
        String fromProperty = System.getProperty(APP_HOME_PROPERTY);
        if (fromProperty != null && !fromProperty.isBlank()) {
            return new AppPaths(Path.of(fromProperty));
        }

        String fromEnv = System.getenv(APP_HOME_ENV);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return new AppPaths(Path.of(fromEnv));
        }

        return new AppPaths(Path.of(System.getProperty("user.home"), "CSPInventory"));
    }

    public void ensureDirectories() throws IOException {
        Files.createDirectories(appHome);
        Files.createDirectories(dataDir);
        Files.createDirectories(modelsDir);
        Files.createDirectories(backupsDir);
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
        return dataDir.resolve("inventory.db");
    }
}
