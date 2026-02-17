package com.cspinventory.app;

public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        StartupDiagnostics.initialize();
        StartupDiagnostics.info("Launcher started");

        // Force software rendering for wider compatibility on Windows GPUs/drivers.
        if (System.getProperty("prism.order") == null || System.getProperty("prism.order").isBlank()) {
            System.setProperty("prism.order", "sw");
            StartupDiagnostics.info("System property prism.order=sw");
        }

        try {
            MainApp.main(args);
        } catch (Throwable throwable) {
            StartupDiagnostics.error("Fatal startup error before UI display", throwable);
            StartupDiagnostics.showFatalDialog(
                    "CSP Inventory - Startup error",
                    "The application failed to start.\nSee log: " + StartupDiagnostics.getLogFile()
            );
            System.exit(1);
        }
    }
}
