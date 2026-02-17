package com.cspinventory.controller;

import com.cspinventory.model.Machine;
import com.cspinventory.service.ExcelExportService;
import com.cspinventory.service.MachineService;
import com.cspinventory.util.AlertUtil;
import com.cspinventory.util.ModelImageResolver;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MainController {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter EXPORT_DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter BACKUP_FILE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter BACKUP_STATUS_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final Duration SEARCH_DEBOUNCE = Duration.millis(180);
    private static final String STATUS_DOT_STYLE_OK = "-fx-text-fill: #16A34A; -fx-font-size: 13px;";
    private static final String STATUS_DOT_STYLE_MAINTENANCE = "-fx-text-fill: #F59E0B; -fx-font-size: 13px;";
    private static final String STATUS_DOT_STYLE_MANQUE = "-fx-text-fill: #DC2626; -fx-font-size: 13px;";
    private static final Set<String> REQUIRED_DB_COLUMNS = Set.of("nomreseau", "site", "lieu");

    @FXML
    private TextField searchField;
    @FXML
    private Label machinesCountLabel;
    @FXML
    private Label databaseStatusLabel;

    @FXML
    private TableView<Machine> machineTable;
    @FXML
    private TableColumn<Machine, String> nomReseauColumn;
    @FXML
    private TableColumn<Machine, String> serieColumn;
    @FXML
    private TableColumn<Machine, String> modelColumn;
    @FXML
    private TableColumn<Machine, String> utilisateurColumn;
    @FXML
    private TableColumn<Machine, String> emplacementColumn;
    @FXML
    private TableColumn<Machine, String> ipv4Column;
    @FXML
    private TableColumn<Machine, String> ipv4WifiColumn;
    @FXML
    private TableColumn<Machine, Boolean> garantieColumn;

    @FXML
    private Label selectedNomReseauLabel;
    @FXML
    private Label selectedSerieLabel;
    @FXML
    private Label selectedModelLabel;
    @FXML
    private Label selectedUtilisateurLabel;
    @FXML
    private Label selectedEmplacementLabel;
    @FXML
    private Label selectedDateMiseEnServiceLabel;
    @FXML
    private Label selectedDateModifLabel;
    @FXML
    private ImageView selectedModelImageView;
    @FXML
    private Label selectedModelImagePlaceholder;
    @FXML
    private Label selectedNoteLabel;

    @FXML
    private Label statusOkCountLabel;
    @FXML
    private Label statusMaintenanceCountLabel;
    @FXML
    private Label statusManqueCountLabel;
    @FXML
    private Label topLieuxLabel;
    @FXML
    private Label siteDistributionLabel;
    @FXML
    private Label garantieCountLabel;
    @FXML
    private Label networkCoverageLabel;
    @FXML
    private Label lastUpdateLabel;

    @FXML
    private Button restoreBackupButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button openSheetButton;

    @FXML
    private StackPane formOverlay;
    @FXML
    private VBox formHost;

    private final ObservableList<Machine> machines = FXCollections.observableArrayList();
    private FilteredList<Machine> filteredMachines;
    private final PauseTransition searchDebounce = new PauseTransition(SEARCH_DEBOUNCE);

    private MachineService machineService;
    private ExcelExportService excelExportService;
    private Path databasePath;

    public void initializeServices(MachineService machineService,
                                   ExcelExportService excelExportService,
                                   Path databasePath) {
        this.machineService = machineService;
        this.excelExportService = excelExportService;
        this.databasePath = databasePath;
        updateDatabaseStatus("Backup session: aucun chargement");

        configureTable();
        configureFiltering();
        configureSelection();
        try {
            loadData();
        } catch (Exception e) {
            AlertUtil.error("Chargement", "Impossible de charger les machines: " + e.getMessage());
        }
        clearQuickSheet();
    }

    private void configureTable() {
        machineTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        nomReseauColumn.setCellValueFactory(data -> wrapped(data.getValue().getNomReseau()));
        nomReseauColumn.setCellFactory(column -> new StatusNetworkCell());

        serieColumn.setCellValueFactory(data -> wrapped(data.getValue().getSerieNmb()));
        modelColumn.setCellValueFactory(data -> wrapped(data.getValue().getModel()));
        utilisateurColumn.setCellValueFactory(data -> wrapped(data.getValue().getUtilisateur()));
        emplacementColumn.setCellValueFactory(data -> wrapped(data.getValue().getEmplacement()));
        ipv4Column.setCellValueFactory(data -> wrapped(data.getValue().getIpv4RJ45()));
        ipv4WifiColumn.setCellValueFactory(data -> wrapped(data.getValue().getIpv4Wifi()));

        garantieColumn.setCellValueFactory(data -> new SimpleBooleanProperty(data.getValue().isGarantie()));
        garantieColumn.setCellFactory(CheckBoxTableCell.forTableColumn(garantieColumn));

        filteredMachines = new FilteredList<>(machines, machine -> true);
        machineTable.setItems(filteredMachines);

        machineTable.setRowFactory(table -> {
            TableRow<Machine> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    onOpenRequested(row.getItem());
                }
            });
            return row;
        });
    }

    private void configureFiltering() {
        searchDebounce.setOnFinished(event -> applySearchFilter(searchField.getText()));
        searchField.textProperty().addListener((obs, oldValue, newValue) -> searchDebounce.playFromStart());
        applySearchFilter(searchField.getText());
    }

    private void configureSelection() {
        deleteButton.disableProperty().bind(machineTable.getSelectionModel().selectedItemProperty().isNull());
        openSheetButton.disableProperty().bind(machineTable.getSelectionModel().selectedItemProperty().isNull());

        machineTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                clearQuickSheet();
            } else {
                fillQuickSheet(newVal);
            }
        });
    }

    private void loadData() {
        List<Machine> all = machineService.findAll();
        machines.setAll(all);
        updateCount();
        updateWidgets(all);
    }

    private void applySearchFilter(String searchText) {
        filteredMachines.setPredicate(machine -> machineService.matches(machine, searchText));
        updateCount();
    }

    private void updateCount() {
        machinesCountLabel.setText(filteredMachines.size() + " machines affichees / " + machines.size() + " total");
    }

    private void updateWidgets(List<Machine> all) {
        long okCount = 0;
        long maintenanceCount = 0;
        long manqueCount = 0;
        long garantie = 0;
        long noUser = 0;
        long rj45 = 0;
        long wifi = 0;
        long both = 0;

        Map<String, Long> lieuxCount = new HashMap<>();
        Map<String, Long> sitesCount = new HashMap<>();
        LocalDateTime maxUpdate = null;

        for (Machine machine : all) {
            String statut = machine.getStatut();
            if (statut == null || statut.isBlank() || "Ok".equalsIgnoreCase(statut)) {
                okCount++;
            } else if ("Maintenance".equalsIgnoreCase(statut)) {
                maintenanceCount++;
            } else if ("Manque".equalsIgnoreCase(statut)) {
                manqueCount++;
            }

            if (machine.isGarantie()) {
                garantie++;
            }

            String utilisateur = machine.getUtilisateur();
            if (utilisateur == null || utilisateur.isBlank()) {
                noUser++;
            }

            String ipv4RJ45 = machine.getIpv4RJ45();
            String ipv4Wifi = machine.getIpv4Wifi();
            boolean hasRJ45 = ipv4RJ45 != null && !ipv4RJ45.isBlank();
            boolean hasWifi = ipv4Wifi != null && !ipv4Wifi.isBlank();

            if (hasRJ45) {
                rj45++;
            }
            if (hasWifi) {
                wifi++;
            }
            if (hasRJ45 && hasWifi) {
                both++;
            }

            addCount(lieuxCount, machine.getLieu());
            addCount(sitesCount, machine.getSite());

            LocalDateTime dateModif = machine.getDateModif();
            if (dateModif != null && (maxUpdate == null || dateModif.isAfter(maxUpdate))) {
                maxUpdate = dateModif;
            }
        }

        statusOkCountLabel.setText(String.valueOf(okCount));
        statusMaintenanceCountLabel.setText(String.valueOf(maintenanceCount));
        statusManqueCountLabel.setText(String.valueOf(manqueCount));
        topLieuxLabel.setText(formatTop(lieuxCount, 5));
        siteDistributionLabel.setText(formatTop(sitesCount, 5));
        garantieCountLabel.setText("Garantie: " + garantie + " / " + all.size() + "\nSans utilisateur: " + noUser);
        networkCoverageLabel.setText("RJ45: " + rj45 + " | Wifi: " + wifi + "\nDouble reseau: " + both);
        lastUpdateLabel.setText("Derniere maj globale: " + formatDateTime(maxUpdate));
    }

    private String formatTop(Map<String, Long> map, int limit) {
        if (map.isEmpty()) {
            return "Aucune donnee";
        }
        return map.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .limit(limit)
                .map(e -> "• " + e.getKey() + " (" + e.getValue() + ")")
                .collect(Collectors.joining("\n"));
    }

    private void addCount(Map<String, Long> counter, String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        counter.merge(key, 1L, Long::sum);
    }

    private void fillQuickSheet(Machine machine) {
        selectedNomReseauLabel.setText(defaultText(machine.getNomReseau()));
        selectedSerieLabel.setText(defaultText(machine.getSerieNmb()));
        selectedModelLabel.setText(defaultText(machine.getModel()));
        selectedUtilisateurLabel.setText(defaultText(machine.getUtilisateur()));
        selectedEmplacementLabel.setText(defaultText(machine.getEmplacement()));
        selectedDateMiseEnServiceLabel.setText(formatDate(machine.getDateMiseEnService()));
        selectedDateModifLabel.setText(formatDateTime(machine.getDateModif()));
        applyModelImage(machine.getModel());
        selectedNoteLabel.setText(defaultText(machine.getNote()));
    }

    private void clearQuickSheet() {
        selectedNomReseauLabel.setText("-");
        selectedSerieLabel.setText("-");
        selectedModelLabel.setText("-");
        selectedUtilisateurLabel.setText("-");
        selectedEmplacementLabel.setText("-");
        selectedDateMiseEnServiceLabel.setText("-");
        selectedDateModifLabel.setText("-");
        selectedModelImageView.setImage(null);
        selectedModelImagePlaceholder.setVisible(true);
        selectedModelImagePlaceholder.setManaged(true);
        selectedModelImagePlaceholder.setText("Apercu modele");
        selectedNoteLabel.setText("-");
    }

    private void applyModelImage(String model) {
        Image image = ModelImageResolver.resolve(model);
        if (image != null) {
            selectedModelImageView.setImage(image);
            selectedModelImagePlaceholder.setVisible(false);
            selectedModelImagePlaceholder.setManaged(false);
            return;
        }

        selectedModelImageView.setImage(null);
        selectedModelImagePlaceholder.setVisible(true);
        selectedModelImagePlaceholder.setManaged(true);
        selectedModelImagePlaceholder.setText(model != null && !model.isBlank() ? model : "Apercu modele");
    }

    @FXML
    private void onRefresh() {
        try {
            loadData();
        } catch (Exception e) {
            AlertUtil.error("Rafraichir", e.getMessage());
        }
    }

    @FXML
    private void onOpenSelectedMachine() {
        onOpenRequested(machineTable.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void onNewMachine() {
        showMachineForm(null);
    }

    @FXML
    private void onRestoreBackup() {
        if (databasePath == null) {
            AlertUtil.error("Restauration backup", "Base active introuvable.");
            return;
        }

        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Selectionner une sauvegarde SQLite");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Backup SQLite", "*.db", "*.sqlite", "*.sqlite3")
            );
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"));

            Path initialDirectory = databasePath.getParent();
            if (initialDirectory != null && Files.isDirectory(initialDirectory)) {
                chooser.setInitialDirectory(initialDirectory.toFile());
            }

            Stage stage = (Stage) machineTable.getScene().getWindow();
            var selected = chooser.showOpenDialog(stage);
            if (selected == null) {
                return;
            }

            Path selectedBackup = selected.toPath().toAbsolutePath().normalize();
            if (selectedBackup.equals(databasePath.toAbsolutePath().normalize())) {
                AlertUtil.error("Restauration backup", "Le fichier selectionne est deja la base active.");
                return;
            }

            validateBackupDatabase(selectedBackup);

            boolean confirmed = AlertUtil.confirm(
                    "Confirmation restauration",
                    "Charger ce backup et remplacer la base active ?\n\n" + selectedBackup
            );
            if (!confirmed) {
                return;
            }

            restoreDatabaseFile(selectedBackup);
            ModelImageResolver.clearCache();
            machineTable.getSelectionModel().clearSelection();
            loadData();
            clearQuickSheet();
            updateDatabaseStatus("Backup charge le " + LocalDateTime.now().format(BACKUP_STATUS_FMT)
                    + " depuis " + formatBackupSource(selectedBackup));
            AlertUtil.info("OK", "Backup charge avec succes. La nouvelle base est active.");
        } catch (Exception e) {
            AlertUtil.error("Restauration backup", e.getMessage());
        }
    }

    private void onOpenRequested(Machine machine) {
        if (machine == null) {
            return;
        }
        if (machine.getNomReseau() == null || machine.getNomReseau().isBlank()) {
            AlertUtil.error("Erreur", "NomReseau manquant.");
            return;
        }
        showMachineForm(copyMachine(machine));
    }

    @FXML
    private void onExportExcel() {
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Choisir le dossier de destination");

            Stage stage = (Stage) machineTable.getScene().getWindow();
            var targetDirectory = chooser.showDialog(stage);
            if (targetDirectory == null) {
                return;
            }

            String exportDate = LocalDate.now().format(EXPORT_DATE_FMT);
            Path exportDir = targetDirectory.toPath().resolve("export_machines_" + exportDate);
            int suffix = 2;
            while (Files.exists(exportDir)) {
                exportDir = targetDirectory.toPath().resolve("export_machines_" + exportDate + "_" + suffix);
                suffix++;
            }
            Files.createDirectories(exportDir);

            Path excelPath = exportDir.resolve("Machines.xlsx");
            excelExportService.export(machineService.findAll(), excelPath);

            if (databasePath != null && Files.exists(databasePath)) {
                Path databaseCopy = exportDir.resolve(databasePath.getFileName().toString());
                Files.copy(databasePath, databaseCopy, StandardCopyOption.REPLACE_EXISTING);
            }

            AlertUtil.info("OK", "Export termine dans:\n" + exportDir.toAbsolutePath());
        } catch (Exception e) {
            AlertUtil.error("Erreur export", e.getMessage());
        }
    }

    @FXML
    private void onDeleteMachine() {
        Machine selected = machineTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        boolean confirmed = AlertUtil.confirm(
                "Confirmation suppression",
                "Supprimer la machine '" + selected.getNomReseau() + "' ?"
        );
        if (confirmed) {
            try {
                machineService.delete(selected.getId());
                AlertUtil.info("OK", "Machine supprimee.");
                loadData();
            } catch (Exception e) {
                AlertUtil.error("Suppression machine", e.getMessage());
            }
        }
    }

    private void validateBackupDatabase(Path backupFile) throws IOException {
        if (backupFile == null || !Files.isRegularFile(backupFile)) {
            throw new IllegalArgumentException("Le fichier de backup est introuvable.");
        }
        if (Files.size(backupFile) == 0) {
            throw new IllegalArgumentException("Le fichier de backup est vide.");
        }

        String jdbcUrl = "jdbc:sqlite:" + backupFile.toAbsolutePath();
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             Statement statement = conn.createStatement()) {

            if (!machinesTableExists(statement)) {
                throw new IllegalArgumentException("La table 'Machines' est absente dans ce backup.");
            }

            Set<String> columns = readMachinesColumns(statement);
            for (String required : REQUIRED_DB_COLUMNS) {
                if (!columns.contains(required)) {
                    throw new IllegalArgumentException("Colonne obligatoire manquante: " + required);
                }
            }

            try (ResultSet rs = statement.executeQuery("SELECT COUNT(1) FROM Machines")) {
                rs.next();
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException("Backup SQLite invalide: " + e.getMessage(), e);
        }
    }

    private boolean machinesTableExists(Statement statement) throws SQLException {
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND lower(name)='machines'";
        try (ResultSet rs = statement.executeQuery(sql)) {
            return rs.next();
        }
    }

    private Set<String> readMachinesColumns(Statement statement) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (ResultSet rs = statement.executeQuery("PRAGMA table_info('Machines')")) {
            while (rs.next()) {
                String name = rs.getString("name");
                if (name != null) {
                    columns.add(name.toLowerCase(Locale.ROOT));
                }
            }
        }
        return columns;
    }

    private void restoreDatabaseFile(Path selectedBackup) throws IOException {
        Path dbDirectory = databasePath.getParent();
        if (dbDirectory == null) {
            throw new IOException("Dossier base introuvable.");
        }
        Files.createDirectories(dbDirectory);

        Path temporaryDb = dbDirectory.resolve("inventory.restore.tmp.db");
        Files.copy(selectedBackup, temporaryDb, StandardCopyOption.REPLACE_EXISTING);

        try {
            validateBackupDatabase(temporaryDb);
            backupCurrentDatabaseBeforeRestore();
            deleteSidecarFiles(databasePath);
            replaceDatabaseFile(temporaryDb, databasePath);
            deleteSidecarFiles(databasePath);
        } finally {
            Files.deleteIfExists(temporaryDb);
            deleteSidecarFiles(temporaryDb);
        }
    }

    private void backupCurrentDatabaseBeforeRestore() throws IOException {
        if (!Files.exists(databasePath) || Files.size(databasePath) == 0) {
            return;
        }
        Path backupsDirectory = resolveBackupsDirectory();
        Files.createDirectories(backupsDirectory);
        String fileName = "before_restore_" + LocalDateTime.now().format(BACKUP_FILE_FMT) + ".db";
        Path backupTarget = backupsDirectory.resolve(fileName);
        Files.copy(databasePath, backupTarget, StandardCopyOption.REPLACE_EXISTING);
    }

    private Path resolveBackupsDirectory() {
        Path dataDirectory = databasePath.getParent();
        if (dataDirectory == null) {
            return Path.of("backups");
        }
        Path appDirectory = dataDirectory.getParent();
        if (appDirectory == null) {
            return dataDirectory.resolve("backups");
        }
        return appDirectory.resolve("backups");
    }

    private void replaceDatabaseFile(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void deleteSidecarFiles(Path dbFile) throws IOException {
        Files.deleteIfExists(Path.of(dbFile.toString() + "-wal"));
        Files.deleteIfExists(Path.of(dbFile.toString() + "-shm"));
    }

    private void updateDatabaseStatus(String details) {
        if (databaseStatusLabel == null) {
            return;
        }
        databaseStatusLabel.setText((details == null || details.isBlank())
                ? "Backup session: aucun chargement"
                : details);

        if (databasePath != null) {
            databaseStatusLabel.setTooltip(new Tooltip("Base active: " + databasePath.toAbsolutePath()));
        }
    }

    private String formatBackupSource(Path backupPath) {
        if (backupPath == null) {
            return "-";
        }
        Path parent = backupPath.getParent();
        if (parent == null || parent.getFileName() == null) {
            return backupPath.getFileName() != null ? backupPath.getFileName().toString() : backupPath.toString();
        }
        String fileName = backupPath.getFileName() != null ? backupPath.getFileName().toString() : backupPath.toString();
        return parent.getFileName() + "/" + fileName;
    }

    private void showMachineForm(Machine machineToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cspinventory/machine-form-view.fxml"));
            Parent root = loader.load();

            MachineFormController controller = loader.getController();
            controller.initialize(machineToEdit, machineService, new MachineFormController.FormCallbacks() {
                @Override
                public void onSave(Machine machine) {
                    try {
                        if (machine.getId() == null) {
                            machineService.create(machine);
                        } else {
                            machineService.update(machine);
                            AlertUtil.info("OK", "Machine sauvegardee.");
                        }
                        hideMachineForm();
                        loadData();
                    } catch (Exception e) {
                        AlertUtil.error("Sauvegarde machine", e.getMessage());
                    }
                }

                @Override
                public void onCancel() {
                    hideMachineForm();
                }

                @Override
                public void onDelete(Machine machine) {
                    if (machine == null || machine.getId() == null) {
                        return;
                    }
                    try {
                        machineService.delete(machine.getId());
                        AlertUtil.info("OK", "Machine supprimee.");
                        hideMachineForm();
                        loadData();
                    } catch (Exception e) {
                        AlertUtil.error("Suppression machine", e.getMessage());
                    }
                }
            });

            formHost.getChildren().setAll(root);
            formOverlay.setManaged(true);
            formOverlay.setVisible(true);
        } catch (IOException e) {
            AlertUtil.error("Formulaire", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void hideMachineForm() {
        formOverlay.setVisible(false);
        formOverlay.setManaged(false);
        formHost.getChildren().clear();
    }

    private ReadOnlyStringWrapper wrapped(String value) {
        return new ReadOnlyStringWrapper(value == null ? "" : value);
    }

    private String formatDate(LocalDate date) {
        return date == null ? "-" : date.format(DATE_FMT);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "-" : dateTime.format(DATETIME_FMT);
    }

    private String defaultText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private Machine copyMachine(Machine source) {
        Machine m = new Machine();
        m.setId(source.getId());
        m.setNomReseau(source.getNomReseau());
        m.setSerieNmb(source.getSerieNmb());
        m.setModel(source.getModel());
        m.setUtilisateur(source.getUtilisateur());
        m.setEmplacement(source.getEmplacement());
        m.setSite(source.getSite());
        m.setLieu(source.getLieu());
        m.setIpv4RJ45(source.getIpv4RJ45());
        m.setIpv4Wifi(source.getIpv4Wifi());
        m.setMacEthernet(source.getMacEthernet());
        m.setMacWifi(source.getMacWifi());
        m.setVlan(source.getVlan());
        m.setGarantie(source.isGarantie());
        m.setStatut(source.getStatut());
        m.setNote(source.getNote());
        m.setPurchaseDate(source.getPurchaseDate());
        m.setDateMiseEnService(source.getDateMiseEnService());
        m.setDateModif(source.getDateModif());
        return m;
    }

    private static class StatusNetworkCell extends TableCell<Machine, String> {

        private final Label nameLabel = new Label();
        private final Label statusDot = new Label("●");
        private final HBox box = new HBox(6, nameLabel, statusDot);

        private StatusNetworkCell() {
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            statusDot.setStyle(STATUS_DOT_STYLE_OK);
            statusDot.setVisible(true);
            statusDot.setManaged(true);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                setGraphic(null);
                return;
            }

            Machine machine = (Machine) getTableRow().getItem();
            nameLabel.setText(item == null ? "" : item);

            String statut = machine.getStatut();
            if (statut != null && statut.equalsIgnoreCase("Maintenance")) {
                statusDot.setStyle(STATUS_DOT_STYLE_MAINTENANCE);
            } else if (statut != null && statut.equalsIgnoreCase("Manque")) {
                statusDot.setStyle(STATUS_DOT_STYLE_MANQUE);
            } else {
                statusDot.setStyle(STATUS_DOT_STYLE_OK);
            }
            setGraphic(box);
        }
    }
}
