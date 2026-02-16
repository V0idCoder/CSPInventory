package com.cspinventory.controller;

import com.cspinventory.model.Machine;
import com.cspinventory.service.MachineService;
import com.cspinventory.util.AlertUtil;
import com.cspinventory.util.EmplacementCatalog;
import com.cspinventory.util.ModelImageResolver;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MachineFormController {

    private static final DateTimeFormatter AUDIT_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @FXML
    private TextField nomReseauField;
    @FXML
    private TextField serieNmbField;
    @FXML
    private TextField modelField;
    @FXML
    private TextField utilisateurField;
    @FXML
    private TextField emplacementField;
    @FXML
    private Label siteLabel;
    @FXML
    private ComboBox<String> siteField;
    @FXML
    private Label lieuLabel;
    @FXML
    private ComboBox<String> lieuField;
    @FXML
    private TextField ipv4RJ45Field;
    @FXML
    private TextField ipv4WifiField;
    @FXML
    private TextField macEthernetField;
    @FXML
    private TextField macWifiField;
    @FXML
    private TextField vlanField;
    @FXML
    private CheckBox garantieField;
    @FXML
    private ComboBox<String> statutField;
    @FXML
    private TextArea noteField;
    @FXML
    private DatePicker purchaseDateField;
    @FXML
    private DatePicker dateMiseEnServiceField;
    @FXML
    private Label titleLabel;
    @FXML
    private Label statusDotLabel;
    @FXML
    private Label subtitleLabel;
    @FXML
    private ImageView modelPreviewImageView;
    @FXML
    private Label modelPreviewLabel;
    @FXML
    private Label modelValueLabel;
    @FXML
    private Label auditStateLabel;
    @FXML
    private Label auditDateModifLabel;
    @FXML
    private Button actionButton;
    @FXML
    private Button deleteButton;

    private Machine initialMachine;
    private MachineService machineService;
    private FormCallbacks formCallbacks;

    public interface FormCallbacks {
        void onSave(Machine machine);

        void onCancel();

        void onDelete(Machine machine);
    }

    public void initialize(Machine machine, MachineService machineService, FormCallbacks formCallbacks) {
        this.initialMachine = machine;
        this.machineService = machineService;
        this.formCallbacks = formCallbacks;

        siteField.setItems(FXCollections.observableArrayList(EmplacementCatalog.SITES));
        lieuField.setItems(FXCollections.observableArrayList());
        statutField.setItems(FXCollections.observableArrayList("Ok", "Maintenance", "Manque"));

        if (machine != null) {
            nomReseauField.setText(machine.getNomReseau());
            serieNmbField.setText(machine.getSerieNmb());
            modelField.setText(machine.getModel());
            utilisateurField.setText(machine.getUtilisateur());
            emplacementField.setText(machine.getEmplacement());
            ipv4RJ45Field.setText(machine.getIpv4RJ45());
            ipv4WifiField.setText(machine.getIpv4Wifi());
            macEthernetField.setText(machine.getMacEthernet());
            macWifiField.setText(machine.getMacWifi());
            vlanField.setText(machine.getVlan());
            garantieField.setSelected(machine.isGarantie());
            statutField.setValue(machine.getStatut());
            noteField.setText(machine.getNote());
            purchaseDateField.setValue(machine.getPurchaseDate());
            dateMiseEnServiceField.setValue(machine.getDateMiseEnService());
        } else {
            statutField.setValue("Ok");
        }

        boolean editMode = machine != null && machine.getId() != null;
        deleteButton.setVisible(editMode);
        deleteButton.setManaged(editMode);
        actionButton.setText(editMode ? "Sauvegarder" : "Creer");
        subtitleLabel.setText(editMode ? "Modification" : "Creation");
        auditStateLabel.setText(editMode ? "Derniere sauvegarde" : "Non sauvegarde");
        auditDateModifLabel.setText(formatAuditDate(editMode ? machine.getDateModif() : null));

        configureEmplacementMode(machine);

        nomReseauField.textProperty().addListener((obs, oldValue, newValue) -> refreshHeader());
        modelField.textProperty().addListener((obs, oldValue, newValue) -> refreshModelPreview());
        statutField.valueProperty().addListener((obs, oldValue, newValue) -> refreshStatusDot());
        siteField.valueProperty().addListener((obs, oldValue, newValue) -> onSiteChanged());
        lieuField.valueProperty().addListener((obs, oldValue, newValue) -> refreshEmplacement());
        if (statutField.getEditor() != null) {
            statutField.getEditor().textProperty().addListener((obs, oldValue, newValue) -> refreshStatusDot());
        }

        actionButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> clean(nomReseauField.getText()) == null,
                nomReseauField.textProperty()
        ));

        refreshHeader();
        refreshModelPreview();
        refreshStatusDot();
    }

    @FXML
    private void onSave() {
        try {
            Machine machine = initialMachine == null ? new Machine() : initialMachine;
            machine.setNomReseau(clean(nomReseauField.getText()));
            machine.setSerieNmb(clean(serieNmbField.getText()));
            machine.setModel(clean(modelField.getText()));
            machine.setUtilisateur(clean(utilisateurField.getText()));

            machine.setSite(clean(siteField.getValue()));
            machine.setLieu(clean(lieuField.getValue()));
            machine.setEmplacement(buildEmplacement(machine.getSite(), machine.getLieu()));

            machine.setIpv4RJ45(clean(ipv4RJ45Field.getText()));
            machine.setIpv4Wifi(clean(ipv4WifiField.getText()));
            machine.setMacEthernet(clean(macEthernetField.getText()));
            machine.setMacWifi(clean(macWifiField.getText()));
            machine.setVlan(clean(vlanField.getText()));
            machine.setGarantie(garantieField.isSelected());
            machine.setStatut(clean(statutField.getValue()));
            machine.setNote(clean(noteField.getText()));
            machine.setPurchaseDate(purchaseDateField.getValue());
            machine.setDateMiseEnService(dateMiseEnServiceField.getValue());

            if (machine.getNomReseau() == null || machine.getNomReseau().isBlank()) {
                throw new IllegalArgumentException("Le champ NomReseau est obligatoire.");
            }
            if (machine.getSite() == null || machine.getSite().isBlank()) {
                throw new IllegalArgumentException("Le champ Site est obligatoire.");
            }
            if (machine.getLieu() == null || machine.getLieu().isBlank()) {
                throw new IllegalArgumentException("Le champ Lieu est obligatoire.");
            }

            Long excludeId = machine.getId();
            if (machineService != null && machineService.isNomReseauTaken(machine.getNomReseau(), excludeId)) {
                throw new IllegalArgumentException("Le NomReseau existe deja.");
            }

            if (machine.getStatut() == null || machine.getStatut().isBlank()) {
                machine.setStatut("Ok");
            }

            if (formCallbacks != null) {
                formCallbacks.onSave(machine);
            }
        } catch (Exception e) {
            AlertUtil.error("Validation", e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        if (formCallbacks != null) {
            formCallbacks.onCancel();
        }
    }

    @FXML
    private void onDelete() {
        if (initialMachine == null || initialMachine.getId() == null) {
            AlertUtil.error("Suppression", "Machine non enregistree, suppression impossible");
            return;
        }

        boolean confirmed = AlertUtil.confirm("Confirmation", "Supprimer cette machine ?");
        if (confirmed) {
            if (formCallbacks != null) {
                formCallbacks.onDelete(initialMachine);
            }
        }
    }

    private void configureEmplacementMode(Machine machine) {
        emplacementField.setEditable(false);
        siteLabel.setVisible(true);
        siteLabel.setManaged(true);
        siteField.setVisible(true);
        siteField.setManaged(true);
        lieuLabel.setVisible(true);
        lieuLabel.setManaged(true);
        lieuField.setVisible(true);
        lieuField.setManaged(true);
        initializeEmplacementSelection(machine);
        refreshEmplacement();
    }

    private void refreshHeader() {
        String nom = clean(nomReseauField.getText());
        if (nom == null) {
            titleLabel.setText(initialMachine == null ? "Nouvelle machine" : "Machine");
        } else {
            titleLabel.setText(nom);
        }
    }

    private void refreshModelPreview() {
        String model = clean(modelField.getText());
        String value = model == null ? "-" : model;
        Image image = ModelImageResolver.resolve(model);
        if (image != null) {
            modelPreviewImageView.setImage(image);
            modelPreviewLabel.setVisible(false);
            modelPreviewLabel.setManaged(false);
        } else {
            modelPreviewImageView.setImage(null);
            modelPreviewLabel.setVisible(true);
            modelPreviewLabel.setManaged(true);
            modelPreviewLabel.setText(value.equals("-") ? "Apercu modele" : value);
        }
        modelValueLabel.setText(value);
    }

    private void refreshStatusDot() {
        String statut = clean(statutField.getValue());
        if (statut == null && statutField.getEditor() != null) {
            statut = clean(statutField.getEditor().getText());
        }

        if ("Maintenance".equalsIgnoreCase(statut)) {
            statusDotLabel.setStyle("-fx-text-fill: #F59E0B; -fx-font-size: 16px;");
        } else if ("Manque".equalsIgnoreCase(statut)) {
            statusDotLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 16px;");
        } else {
            statusDotLabel.setStyle("-fx-text-fill: #16A34A; -fx-font-size: 16px;");
        }
    }

    private void initializeEmplacementSelection(Machine machine) {
        String site = machine != null ? clean(machine.getSite()) : null;
        String lieu = machine != null ? clean(machine.getLieu()) : null;

        if (site == null || lieu == null) {
            String[] parsed = parseEmplacement(machine != null ? machine.getEmplacement() : null);
            if (site == null) {
                site = parsed[0];
            }
            if (lieu == null) {
                lieu = parsed[1];
            }
        }

        if (site != null && !siteField.getItems().contains(site)) {
            siteField.getItems().add(site);
        }
        siteField.setValue(site);
        reloadLieux();

        if (lieu != null && !lieuField.getItems().contains(lieu)) {
            lieuField.getItems().add(lieu);
        }
        lieuField.setValue(lieu);
    }

    private void onSiteChanged() {
        reloadLieux();
        refreshEmplacement();
    }

    private void reloadLieux() {
        String site = clean(siteField.getValue());
        List<String> lieux = site != null
                ? EmplacementCatalog.LIEUX_PAR_SITE.getOrDefault(site, List.of())
                : List.of();

        String previousLieu = clean(lieuField.getValue());
        lieuField.setItems(FXCollections.observableArrayList(new ArrayList<>(lieux)));

        if (previousLieu != null && lieuField.getItems().contains(previousLieu)) {
            lieuField.setValue(previousLieu);
        } else {
            lieuField.setValue(null);
        }
    }

    private void refreshEmplacement() {
        emplacementField.setText(buildEmplacement(clean(siteField.getValue()), clean(lieuField.getValue())));
    }

    private String buildEmplacement(String site, String lieu) {
        if (site == null || lieu == null) {
            return null;
        }
        return site + ", " + lieu;
    }

    private String[] parseEmplacement(String emplacement) {
        if (emplacement == null || emplacement.isBlank()) {
            return new String[]{null, null};
        }
        String[] parts = emplacement.split(",", 2);
        if (parts.length != 2) {
            return new String[]{null, null};
        }
        return new String[]{clean(parts[0]), clean(parts[1])};
    }

    private String formatAuditDate(LocalDateTime dateTime) {
        return dateTime == null ? "-" : dateTime.format(AUDIT_FMT);
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
