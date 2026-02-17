package com.cspinventory.service;

import com.cspinventory.model.Machine;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelExportService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final int AUTO_SIZE_MAX_ROWS = 1500;

    private static final String[] HEADERS = {
            "NomReseau", "SerieNmb", "Model", "Utilisateur", "Emplacement", "Site", "Lieu",
            "IPv4RJ45", "IPv4Wifi", "MACEthernet", "MACWifi", "VLAN", "Garantie", "Statut",
            "Note", "PurchaseDate", "DateMiseEnService", "DateModif"
    };

    public void export(List<Machine> machines, Path destination) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(200)) {
            workbook.setCompressTempFiles(true);
            Sheet sheet = workbook.createSheet("Machines");

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
            }

            int rowIdx = 1;
            for (Machine machine : machines) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(nullSafe(machine.getNomReseau()));
                row.createCell(1).setCellValue(nullSafe(machine.getSerieNmb()));
                row.createCell(2).setCellValue(nullSafe(machine.getModel()));
                row.createCell(3).setCellValue(nullSafe(machine.getUtilisateur()));
                row.createCell(4).setCellValue(nullSafe(machine.getEmplacement()));
                row.createCell(5).setCellValue(nullSafe(machine.getSite()));
                row.createCell(6).setCellValue(nullSafe(machine.getLieu()));
                row.createCell(7).setCellValue(nullSafe(machine.getIpv4RJ45()));
                row.createCell(8).setCellValue(nullSafe(machine.getIpv4Wifi()));
                row.createCell(9).setCellValue(nullSafe(machine.getMacEthernet()));
                row.createCell(10).setCellValue(nullSafe(machine.getMacWifi()));
                row.createCell(11).setCellValue(nullSafe(machine.getVlan()));
                row.createCell(12).setCellValue(machine.isGarantie() ? "Oui" : "Non");
                row.createCell(13).setCellValue(nullSafe(machine.getStatut()));
                row.createCell(14).setCellValue(nullSafe(machine.getNote()));
                row.createCell(15).setCellValue(
                        machine.getPurchaseDate() != null ? machine.getPurchaseDate().format(DATE_FORMATTER) : "");
                row.createCell(16)
                        .setCellValue(machine.getDateMiseEnService() != null
                                ? machine.getDateMiseEnService().format(DATE_FORMATTER)
                                : "");
                row.createCell(17).setCellValue(
                        machine.getDateModif() != null ? machine.getDateModif().format(DATE_TIME_FORMATTER) : "");
            }

            Row totalRow = sheet.createRow(rowIdx + 1);
            totalRow.createCell(0).setCellValue("Total machines");
            totalRow.createCell(1).setCellValue(machines.size());

            sheet.createFreezePane(0, 1);
            resizeColumns(sheet, machines.size());

            if (destination.getParent() != null) {
                Files.createDirectories(destination.getParent());
            }
            try (OutputStream os = Files.newOutputStream(destination)) {
                workbook.write(os);
            }
        } catch (IOException e) {
            throw new RuntimeException("Export Excel impossible", e);
        }
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private void resizeColumns(Sheet sheet, int rowCount) {
        if (rowCount <= AUTO_SIZE_MAX_ROWS) {
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.min(sheet.getColumnWidth(i) + 800, 12000));
            }
            return;
        }

        // Auto-size is expensive on large exports; use stable default widths for speed.
        for (int i = 0; i < HEADERS.length; i++) {
            int baseWidth = Math.max(HEADERS[i].length() * 320, 2800);
            sheet.setColumnWidth(i, Math.min(baseWidth + 600, 12000));
        }
    }
}
