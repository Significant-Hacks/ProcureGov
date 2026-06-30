package com.procuregov.service;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for generating PDF documents using iText library.
 * Produces award confirmation documents for the ProcureGov system.
 */
public class PdfGenerationService {

    private static final Logger LOGGER = Logger.getLogger(PdfGenerationService.class.getName());

    private static final Font FONT_TITLE = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.WHITE);
    private static final Font FONT_SUBTITLE = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(44, 82, 130));
    private static final Font FONT_BODY = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.DARK_GRAY);
    private static final Font FONT_LABEL = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font FONT_VALUE = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, new BaseColor(44, 82, 130));
    private static final Font FONT_FOOTER = new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC, BaseColor.GRAY);
    private static final Font FONT_MINISTRY = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.WHITE);
    private static final BaseColor HEADER_BG = new BaseColor(26, 58, 92);
    private static final BaseColor LIGHT_BG = new BaseColor(240, 242, 245);

    /**
     * Generates an award confirmation PDF document.
     *
     * @param tenderRef      tender reference number (e.g. MPW-2026-0001)
     * @param tenderTitle    tender title
     * @param supplierName   winning supplier company name
     * @param awardedValue   awarded contract value in Maloti
     * @param justification  award justification note
     * @param awardDate      date of award
     * @param outputDir      directory to save the PDF file
     * @return the filename of the generated PDF, or null on failure
     */
    public String generateAwardConfirmationPdf(String tenderRef, String tenderTitle,
                                                 String supplierName, BigDecimal awardedValue,
                                                 String justification, Date awardDate,
                                                 String outputDir) {
        String fileName = "awards/" + tenderRef + "-award-confirmation.pdf";
        File dir = new File(outputDir, "awards");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File pdfFile = new File(outputDir, fileName);

        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        try (OutputStream os = new FileOutputStream(pdfFile)) {
            PdfWriter.getInstance(document, os);
            document.open();

            // Header banner
            PdfPTable headerTable = new PdfPTable(1);
            headerTable.setWidthPercentage(100);
            PdfPCell headerCell = new PdfPCell();
            headerCell.setBackgroundColor(HEADER_BG);
            headerCell.setBorder(Rectangle.NO_BORDER);
            headerCell.setPadding(20);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            Paragraph ministryP = new Paragraph("MINISTRY OF PUBLIC WORKS", FONT_MINISTRY);
            ministryP.setAlignment(Element.ALIGN_CENTER);
            Paragraph kingdomP = new Paragraph("Kingdom of Lesotho", new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, new BaseColor(180, 200, 220)));
            kingdomP.setAlignment(Element.ALIGN_CENTER);
            headerCell.addElement(ministryP);
            headerCell.addElement(kingdomP);
            headerCell.addElement(Chunk.NEWLINE);
            Paragraph titleP = new Paragraph("CONTRACT AWARD CONFIRMATION", FONT_TITLE);
            titleP.setAlignment(Element.ALIGN_CENTER);
            headerCell.addElement(titleP);
            headerTable.addCell(headerCell);
            document.add(headerTable);

            document.add(Chunk.NEWLINE);

            // Reference and date line
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
            Paragraph refLine = new Paragraph();
            refLine.add(new Phrase("Reference: ", FONT_LABEL));
            refLine.add(new Phrase(tenderRef, FONT_VALUE));
            refLine.add(new Chunk("    "));
            refLine.add(new Phrase("Date: ", FONT_LABEL));
            refLine.add(new Phrase(sdf.format(awardDate), FONT_VALUE));
            document.add(refLine);

            document.add(Chunk.NEWLINE);

            // Award details table
            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(100);
            detailsTable.setWidths(new float[]{35f, 65f});

            addDetailRow(detailsTable, "Tender Reference:", tenderRef);
            addDetailRow(detailsTable, "Tender Title:", tenderTitle);
            addDetailRow(detailsTable, "Winning Supplier:", supplierName);
            addDetailRow(detailsTable, "Awarded Value:", "M " + String.format("%,.2f", awardedValue));
            addDetailRow(detailsTable, "Award Date:", sdf.format(awardDate));

            document.add(detailsTable);
            document.add(Chunk.NEWLINE);

            // Justification section
            Paragraph justTitle = new Paragraph("Award Justification", FONT_SUBTITLE);
            document.add(justTitle);
            document.add(Chunk.NEWLINE);

            PdfPTable justTable = new PdfPTable(1);
            justTable.setWidthPercentage(100);
            PdfPCell justCell = new PdfPCell(new Phrase(justification, FONT_BODY));
            justCell.setBackgroundColor(LIGHT_BG);
            justCell.setBorder(Rectangle.NO_BORDER);
            justCell.setPadding(12);
            justTable.addCell(justCell);
            document.add(justTable);

            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            // Signature section
            PdfPTable sigTable = new PdfPTable(2);
            sigTable.setWidthPercentage(100);
            sigTable.setWidths(new float[]{50f, 50f});

            PdfPCell sigLeft = new PdfPCell();
            sigLeft.setBorder(Rectangle.NO_BORDER);
            sigLeft.addElement(new Paragraph("_________________________", FONT_BODY));
            sigLeft.addElement(new Paragraph("Procurement Officer", FONT_FOOTER));
            sigLeft.addElement(new Paragraph("Signature & Date", FONT_FOOTER));

            PdfPCell sigRight = new PdfPCell();
            sigRight.setBorder(Rectangle.NO_BORDER);
            sigRight.addElement(new Paragraph("_________________________", FONT_BODY));
            sigRight.addElement(new Paragraph("Director of ICT", FONT_FOOTER));
            sigRight.addElement(new Paragraph("Approval & Date", FONT_FOOTER));

            sigTable.addCell(sigLeft);
            sigTable.addCell(sigRight);
            document.add(sigTable);

            document.add(Chunk.NEWLINE);

            // Footer
            Paragraph footer = new Paragraph();
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.add(new Phrase("This document was auto-generated by the ProcureGov Tender Management System.", FONT_FOOTER));
            document.add(footer);

            document.close();
            LOGGER.info("Award confirmation PDF generated: " + fileName);
            return fileName;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating award confirmation PDF", e);
            if (pdfFile.exists()) {
                pdfFile.delete();
            }
            return null;
        }
    }

    /**
     * Adds a label-value row to a detail table.
     */
    private void addDetailRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, FONT_LABEL));
        labelCell.setBackgroundColor(LIGHT_BG);
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(8);
        labelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, FONT_VALUE));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(8);
        valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    /**
     * Inner class for A4 page size convenience.
     */
    private static final Rectangle PageSize_A4 = com.itextpdf.text.PageSize.A4;
}
