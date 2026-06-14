package com.sagem.g2ii.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font; // Font exclusif pour OpenPDF
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Cell;
import com.lowagie.text.Table; // 💡 Remplacement de PdfTable par Table
import com.lowagie.text.pdf.PdfWriter;
import com.sagem.g2ii.Entity.Intervention.Ticket;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class ExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * 📄 Générer un PDF du ticket avec OpenPDF
     */
    public byte[] generateTicketPDF(Ticket ticket) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, baos);
        document.open();

        // ===== STYLE TITLE (OpenPDF) =====
        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
        Font normalFont = new Font(Font.HELVETICA, 10);

        // ===== EN-TÊTE =====
        Paragraph title = new Paragraph("RAPPORT DE TICKET D'INTERVENTION", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph subTitle = new Paragraph("Référence: " + ticket.getReference(), headerFont);
        subTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(subTitle);
        document.add(new Paragraph("\n"));

        // ===== INFORMATIONS GÉNÉRALES =====
        document.add(new Paragraph("INFORMATIONS GÉNÉRALES", headerFont));

        // 💡 Utilisation de la classe publique Table
        Table generalTable = new Table(2);

        addTableRow(generalTable, "Référence:", ticket.getReference());
        addTableRow(generalTable, "Titre:", ticket.getTitre());
        addTableRow(generalTable, "Statut:", ticket.getStatut() != null ? ticket.getStatut().toString() : "N/A");
        addTableRow(generalTable, "Priorité:", ticket.getPriorite() != null ? ticket.getPriorite().toString() : "N/A");
        addTableRow(generalTable, "Catégorie:", ticket.getCategorie() != null ? ticket.getCategorie().getNomCategorie() : "N/A");

        // 💡 Correction: On récupère le String (nom du groupe) et non l'objet
// 💡 Ajout de .name() pour convertir l'Enum en String
        addTableRow(generalTable, "Groupe Assigné:",
                (ticket.getGroupeAssigne() != null && ticket.getGroupeAssigne().getNomGroupes() != null)
                        ? ticket.getGroupeAssigne().getNomGroupes().name()
                        : "Non assigné"
        );
        addTableRow(generalTable, "Date de Création:", ticket.getDate() != null ? ticket.getDate().format(DATE_FORMATTER) : "N/A");

        document.add(generalTable);
        document.add(new Paragraph("\n"));

        // ===== DESCRIPTION =====
        document.add(new Paragraph("DESCRIPTION", headerFont));
        document.add(new Paragraph(ticket.getDescription() != null ? ticket.getDescription() : "Aucune description", normalFont));
        document.add(new Paragraph("\n"));

        // ===== DEMANDEUR =====
        document.add(new Paragraph("DEMANDEUR", headerFont));
        if (ticket.getDemandeur() != null) {
            document.add(new Paragraph("Nom: " + ticket.getDemandeur().getPrenom() + " " + ticket.getDemandeur().getNom(), normalFont));
            document.add(new Paragraph("Email: " + ticket.getDemandeur().getEmail(), normalFont));
            if (ticket.getDemandeur().getDepartement() != null) {
                document.add(new Paragraph("Département: " + ticket.getDemandeur().getDepartement().toString(), normalFont));
            }
        }
        document.add(new Paragraph("\n"));

        // ===== RÉSOLUTION =====
        if (ticket.getStatut() != null &&
                (ticket.getStatut().toString().equals("Resolu") || ticket.getStatut().toString().equals("Cloture"))) {
            document.add(new Paragraph("RÉSOLUTION", headerFont));
            if (ticket.getNoteResolution() != null) {
                document.add(new Paragraph(ticket.getNoteResolution(), normalFont));
            }
            if (ticket.getDateResolution() != null) {
                document.add(new Paragraph("Date de Résolution: " + ticket.getDateResolution(), normalFont));
            }
            if (ticket.getDelaiResolution() > 0) {
                document.add(new Paragraph("Délai: " + ticket.getDelaiResolution() + " minutes", normalFont));
            }
            document.add(new Paragraph("SLA Respecté: " + (ticket.getSlaRespecte() ? "✓ OUI" : "✗ NON"), normalFont));
            document.add(new Paragraph("\n"));
        }

        // ===== FOOTER =====
        document.add(new Paragraph("\n"));
        Paragraph footer = new Paragraph("Document généré le " + java.time.LocalDateTime.now().format(DATE_FORMATTER), new Font(Font.HELVETICA, 9));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
        return baos.toByteArray();
    }

    /**
     * 📊 Générer un Excel du ticket avec Apache POI
     */
    public byte[] generateTicketExcel(Ticket ticket) throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Ticket_" + (ticket.getReference() != null ? ticket.getReference() : "Export"));

        // Style du header
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 💡 Utilisation explicite de org.apache.poi.ss.usermodel.Font + correction isBold -> setBold
        org.apache.poi.ss.usermodel.Font headerFontPOI = workbook.createFont();
        headerFontPOI.setColor(IndexedColors.WHITE.getIndex());
        headerFontPOI.setBold(true);
        headerFontPOI.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFontPOI);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        // Style de clé (gras)
        CellStyle keyStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font keyFontPOI = workbook.createFont();
        keyFontPOI.setBold(true);
        keyStyle.setFont(keyFontPOI);
        keyStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        keyStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // ===== SECTION TITRE =====
        int rowNum = 0;
        Row titleRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("RAPPORT DE TICKET D'INTERVENTION");
        titleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 1));

        rowNum++; // Ligne vide

        // ===== INFORMATIONS GÉNÉRALES =====
        rowNum = addExcelSection(sheet, rowNum, "INFORMATIONS GÉNÉRALES", keyStyle);
        rowNum = addExcelRow(sheet, rowNum, "Référence", ticket.getReference(), keyStyle);
        rowNum = addExcelRow(sheet, rowNum, "Titre", ticket.getTitre(), keyStyle);
        rowNum = addExcelRow(sheet, rowNum, "Statut", ticket.getStatut() != null ? ticket.getStatut().toString() : "N/A", keyStyle);
        rowNum = addExcelRow(sheet, rowNum, "Priorité", ticket.getPriorite() != null ? ticket.getPriorite().toString() : "N/A", keyStyle);
        rowNum = addExcelRow(sheet, rowNum, "Catégorie", ticket.getCategorie() != null ? ticket.getCategorie().getNomCategorie() : "N/A", keyStyle);

        // 💡 Extraction correcte en String
// 💡 Ajout de .name() pour convertir l'Enum en String
        rowNum = addExcelRow(sheet, rowNum, "Groupe Assigné",
                (ticket.getGroupeAssigne() != null && ticket.getGroupeAssigne().getNomGroupes() != null)
                        ? ticket.getGroupeAssigne().getNomGroupes().name()
                        : "Non assigné",
                keyStyle
        );
        rowNum = addExcelRow(sheet, rowNum, "Date de Création", ticket.getDate() != null ? ticket.getDate().format(DATE_FORMATTER) : "N/A", keyStyle);

        rowNum++; // Ligne vide

        // ===== DESCRIPTION =====
        rowNum = addExcelSection(sheet, rowNum, "DESCRIPTION", keyStyle);
        rowNum = addExcelRow(sheet, rowNum, "Contenu", ticket.getDescription() != null ? ticket.getDescription() : "Aucune description", keyStyle);

        rowNum++; // Ligne vide

        // ===== DEMANDEUR =====
        rowNum = addExcelSection(sheet, rowNum, "DEMANDEUR", keyStyle);
        if (ticket.getDemandeur() != null) {
            rowNum = addExcelRow(sheet, rowNum, "Nom", ticket.getDemandeur().getPrenom() + " " + ticket.getDemandeur().getNom(), keyStyle);
            rowNum = addExcelRow(sheet, rowNum, "Email", ticket.getDemandeur().getEmail(), keyStyle);
            rowNum = addExcelRow(sheet, rowNum, "Département", ticket.getDemandeur().getDepartement() != null ? ticket.getDemandeur().getDepartement().toString() : "N/A", keyStyle);
        }

        rowNum++; // Ligne vide

        // ===== RÉSOLUTION =====
        if (ticket.getStatut() != null &&
                (ticket.getStatut().toString().equals("Resolu") || ticket.getStatut().toString().equals("Cloture"))) {
            rowNum = addExcelSection(sheet, rowNum, "RÉSOLUTION", keyStyle);
            rowNum = addExcelRow(sheet, rowNum, "Date de Résolution", ticket.getDateResolution() != null ? ticket.getDateResolution().toString() : "N/A", keyStyle);
            rowNum = addExcelRow(sheet, rowNum, "Délai (minutes)", String.valueOf(ticket.getDelaiResolution()), keyStyle);
            rowNum = addExcelRow(sheet, rowNum, "SLA Respecté", ticket.getSlaRespecte() != null && ticket.getSlaRespecte() ? "OUI" : "NON", keyStyle);
            rowNum = addExcelRow(sheet, rowNum, "Note de Résolution", ticket.getNoteResolution() != null ? ticket.getNoteResolution() : "", keyStyle);
        }

        // Auto-redimensionner les colonnes
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        return baos.toByteArray();
    }

    // ===== MÉTHODES UTILITAIRES =====

    // 💡 Modification du type: utilisation de Table au lieu de PdfTable
    private void addTableRow(Table table, String key, String value) throws BadElementException {
        Cell keyCell = new Cell(new Paragraph(key, new Font(Font.HELVETICA, 10, Font.BOLD)));
        keyCell.setBackgroundColor(new java.awt.Color(200, 200, 200));
        table.addCell(keyCell);

        Cell valueCell = new Cell(new Paragraph(value != null ? value : "N/A"));
        table.addCell(valueCell);
    }

    private int addExcelSection(Sheet sheet, int rowNum, String section, CellStyle keyStyle) {
        Row row = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);
        cell.setCellValue(section);
        cell.setCellStyle(keyStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));
        return rowNum;
    }

    private int addExcelRow(Sheet sheet, int rowNum, String key, String value, CellStyle keyStyle) {
        Row row = sheet.createRow(rowNum++);

        org.apache.poi.ss.usermodel.Cell keyCell = row.createCell(0);
        keyCell.setCellValue(key);
        keyCell.setCellStyle(keyStyle);

        org.apache.poi.ss.usermodel.Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value != null ? value : "");

        return rowNum;
    }
}