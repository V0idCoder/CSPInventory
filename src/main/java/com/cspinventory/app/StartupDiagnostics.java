package com.cspinventory.app;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class StartupDiagnostics {

    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Path LOG_FILE = Path.of(System.getProperty("user.home"), "CSPInventory", "logs", "startup.log");

    private StartupDiagnostics() {
    }

    public static void initialize() {
        ensureLogDirectory();
        info("Startup diagnostics initialized");
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
                error("Unhandled exception on thread " + thread.getName(), throwable));
    }

    public static void info(String message) {
        append("INFO", message, null);
    }

    public static void error(String message, Throwable throwable) {
        append("ERROR", message, throwable);
    }

    public static Path getLogFile() {
        return LOG_FILE;
    }

    public static void showFatalDialog(String title, String body) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // fallback to default look and feel
        }
        JOptionPane.showMessageDialog(null, body, title, JOptionPane.ERROR_MESSAGE);
    }

    private static synchronized void append(String level, String message, Throwable throwable) {
        ensureLogDirectory();
        StringBuilder builder = new StringBuilder();
        builder.append("[")
                .append(LocalDateTime.now().format(TS_FORMAT))
                .append("] ")
                .append(level)
                .append(" ")
                .append(message)
                .append(System.lineSeparator());

        if (throwable != null) {
            StringWriter sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw));
            builder.append(sw).append(System.lineSeparator());
        }

        try {
            Files.writeString(
                    LOG_FILE,
                    builder.toString(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (Exception ignored) {
            // no-op: do not throw from diagnostics
        }
    }

    private static void ensureLogDirectory() {
        try {
            Files.createDirectories(LOG_FILE.getParent());
        } catch (Exception ignored) {
            // no-op: do not throw from diagnostics
        }
    }
}
