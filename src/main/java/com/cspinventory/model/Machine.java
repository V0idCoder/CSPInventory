package com.cspinventory.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Machine {

    private Long id;
    private String nomReseau;
    private String serieNmb;
    private String model;
    private String utilisateur;
    private String emplacement;
    private String site;
    private String lieu;
    private String ipv4RJ45;
    private String ipv4Wifi;
    private String macEthernet;
    private String macWifi;
    private String vlan;
    private boolean garantie;
    private String statut;
    private String note;
    private LocalDate purchaseDate;
    private LocalDate dateMiseEnService;
    private LocalDateTime dateModif;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomReseau() {
        return nomReseau;
    }

    public void setNomReseau(String nomReseau) {
        this.nomReseau = nomReseau;
    }

    public String getSerieNmb() {
        return serieNmb;
    }

    public void setSerieNmb(String serieNmb) {
        this.serieNmb = serieNmb;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(String utilisateur) {
        this.utilisateur = utilisateur;
    }

    public String getEmplacement() {
        return emplacement;
    }

    public void setEmplacement(String emplacement) {
        this.emplacement = emplacement;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public String getIpv4RJ45() {
        return ipv4RJ45;
    }

    public void setIpv4RJ45(String ipv4RJ45) {
        this.ipv4RJ45 = ipv4RJ45;
    }

    public String getIpv4Wifi() {
        return ipv4Wifi;
    }

    public void setIpv4Wifi(String ipv4Wifi) {
        this.ipv4Wifi = ipv4Wifi;
    }

    public String getMacEthernet() {
        return macEthernet;
    }

    public void setMacEthernet(String macEthernet) {
        this.macEthernet = macEthernet;
    }

    public String getMacWifi() {
        return macWifi;
    }

    public void setMacWifi(String macWifi) {
        this.macWifi = macWifi;
    }

    public String getVlan() {
        return vlan;
    }

    public void setVlan(String vlan) {
        this.vlan = vlan;
    }

    public boolean isGarantie() {
        return garantie;
    }

    public void setGarantie(boolean garantie) {
        this.garantie = garantie;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public LocalDate getDateMiseEnService() {
        return dateMiseEnService;
    }

    public void setDateMiseEnService(LocalDate dateMiseEnService) {
        this.dateMiseEnService = dateMiseEnService;
    }

    public LocalDateTime getDateModif() {
        return dateModif;
    }

    public void setDateModif(LocalDateTime dateModif) {
        this.dateModif = dateModif;
    }
}
