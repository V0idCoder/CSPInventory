package com.cspinventory.app;

import com.cspinventory.controller.MainController;
import com.cspinventory.dao.DatabaseManager;
import com.cspinventory.dao.SQLiteMachineDao;
import com.cspinventory.service.ExcelExportService;
import com.cspinventory.service.MachineService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainApp extends Application {

    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());

    @Override
    public void start(Stage primaryStage) {
        configureLogging();
        try {
            DatabaseManager databaseManager = new DatabaseManager("inventory.db");
            databaseManager.initialize();

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
