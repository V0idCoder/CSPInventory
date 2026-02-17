package com.cspinventory.app;

import com.cspinventory.controller.MainController;
import com.cspinventory.dao.DatabaseManager;
import com.cspinventory.dao.SQLiteMachineDao;
import com.cspinventory.service.ExcelExportService;
import com.cspinventory.service.MachineService;
import com.cspinventory.util.AppPaths;
import com.cspinventory.util.ModelImageResolver;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class MainApp extends Application {

    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());
    private static final DateTimeFormatter BACKUP_FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final int MAX_BACKUP_FILES = 20;

    @Override
    public void start(Stage primaryStage) {
        configureLogging();
        try {
            AppPaths appPaths = AppPaths.resolveDefault();
            appPaths.ensureDirectories();

            ModelImageResolver.configureExternalModelsDirectory(appPaths.getModelsDir());
            LOGGER.info("Application home: " + appPaths.getAppHome());

            DatabaseManager databaseManager = new DatabaseManager(appPaths.getDatabasePath().toString());
            databaseManager.initialize();
            backupDatabase(databaseManager.getDbPath(), appPaths.getBackupsDir());

            MachineService machineService = new MachineService(new SQLiteMachineDao(databaseManager));
            ExcelExportService excelExportService = new ExcelExportService();

            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/cspinventory/main-view.fxml"));
            Parent root = loader.load();

            MainController controller = loader.getController();
            controller.initializeServices(machineService, excelExportService, databaseManager.getDbPath());

            Scene scene = new Scene(root, 1400, 760);
            primaryStage.setTitle("CSP Inventory");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1100);
            primaryStage.setMinHeight(620);
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Application startup failed", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Demarrage impossible");
            alert.setHeaderText("Erreur initialisation");
            alert.setContentText("L'application ne peut pas demarrer. Details: " + e.getMessage());
            alert.showAndWait();
            Platform.exit();
        }
    }

    private void backupDatabase(Path dbPath, Path backupDirectory) {
        try {
            if (!Files.exists(dbPath) || Files.size(dbPath) == 0) {
                return;
            }
            String backupFileName = "inventory_" + LocalDateTime.now().format(BACKUP_FMT) + ".db";
            Path backupPath = backupDirectory.resolve(backupFileName);
            Files.copy(dbPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Database backup created: " + backupPath);
            rotateBackups(backupDirectory);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unable to create startup backup", e);
        }
    }

    private void rotateBackups(Path backupDirectory) {
        try (Stream<Path> backupStream = Files.list(backupDirectory)) {
            List<Path> backups = backupStream
                    .filter(path -> path.getFileName().toString().matches("inventory_\\d{8}_\\d{6}\\.db"))
                    .sorted(Comparator.comparing(this::lastModifiedMillis).reversed())
                    .toList();

            for (int i = MAX_BACKUP_FILES; i < backups.size(); i++) {
                Files.deleteIfExists(backups.get(i));
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unable to rotate backups", e);
        }
    }

    private long lastModifiedMillis(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (Exception e) {
            return Long.MIN_VALUE;
        }
    }

    private void configureLogging() {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.INFO);
        for (var handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.INFO);
        rootLogger.addHandler(consoleHandler);
        LOGGER.info("Logging initialized");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
