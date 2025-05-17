package com.example.certificateservice.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;

public class PdfGenerator {

    public static void generate(String userFullName, String courseTitle, String filePath) {
        try {
            // Ensure parent directory exists
            File file = new File(filePath);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            Document document = new Document(PageSize.A4, 50, 50, 60, 60);
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // 1. Top colored bar (VERMEG blue)
            LineSeparator topBar = new LineSeparator();
            topBar.setLineColor(new Color(25, 91, 163));
            topBar.setLineWidth(7);
            document.add(new Chunk(topBar));
            document.add(Chunk.NEWLINE);

            // 2. VERMEG logo centered
            String logoPath = "uploads/vermegnew.jpg"; // Your logo path
            if (new File(logoPath).exists()) {
                Image logo = Image.getInstance(logoPath);
                logo.scaleToFit(110, 55);
                logo.setAlignment(Image.ALIGN_CENTER);
                document.add(logo);
            }
            document.add(Chunk.NEWLINE);

            // 3. Main Title
            Font mainTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 30, new Color(25, 91, 163));
            Paragraph mainTitle = new Paragraph("CERTIFICATE OF COMPLETION", mainTitleFont);
            mainTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(mainTitle);
            document.add(Chunk.NEWLINE);

            // 4. Subtitle
            Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 15);
            Paragraph subTitle = new Paragraph("This certificate is proudly presented to", subFont);
            subTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subTitle);
            document.add(Chunk.NEWLINE);

            // 5. Recipient Name (prominent)
            Font nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, new Color(25, 91, 163));
            Paragraph name = new Paragraph(userFullName, nameFont);
            name.setAlignment(Element.ALIGN_CENTER);
            document.add(name);
            document.add(Chunk.NEWLINE);

            // 6. "Has successfully completed..." message
            Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 14);
            Paragraph info = new Paragraph("has successfully completed the course:", infoFont);
            info.setAlignment(Element.ALIGN_CENTER);
            document.add(info);

            // 7. Course Title (bold)
            Font courseFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 17);
            Paragraph course = new Paragraph(courseTitle, courseFont);
            course.setAlignment(Element.ALIGN_CENTER);
            document.add(course);
            document.add(Chunk.NEWLINE);

            // 8. Issued on date (italic)
            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 12);
            Paragraph date = new Paragraph("Issued on: " + LocalDate.now(), dateFont);
            date.setAlignment(Element.ALIGN_CENTER);
            document.add(date);

            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            // 9. Signature or stamp area (left: signature, right: academy)
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(80);
            table.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.setWidths(new float[] {1, 1});

            // Signature image or placeholder
            PdfPCell signatureCell = new PdfPCell();
            signatureCell.setBorder(Rectangle.NO_BORDER);
            String signPath = "uploads/signature.png"; // Your signature path
            if (new File(signPath).exists()) {
                Image sign = Image.getInstance(signPath);
                sign.scaleToFit(90, 45);
                sign.setAlignment(Image.ALIGN_LEFT);
                signatureCell.addElement(sign);
            } else {
                Paragraph signText = new Paragraph("Signature", infoFont);
                signatureCell.addElement(signText);
            }

            // Academy
            PdfPCell academyCell = new PdfPCell(new Phrase("VERMEG Academy", infoFont));
            academyCell.setBorder(Rectangle.NO_BORDER);
            academyCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

            table.addCell(signatureCell);
            table.addCell(academyCell);
            document.add(table);

            document.add(Chunk.NEWLINE);

            // 10. Bottom colored bar (VERMEG blue)
            LineSeparator bottomBar = new LineSeparator();
            bottomBar.setLineColor(new Color(25, 91, 163));
            bottomBar.setLineWidth(7);
            document.add(new Chunk(bottomBar));

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
