package com.example.certificateservice.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.time.LocalDate;

public class PdfGenerator {

    public static void generate(String userName, String courseTitle, String filePath) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 14);

            document.add(new Paragraph("Certificate of Completion", titleFont));
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("This certifies that " + userName, bodyFont));
            document.add(new Paragraph("has successfully completed the course:", bodyFont));
            document.add(new Paragraph(courseTitle, titleFont));
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("Issued on: " + LocalDate.now(), bodyFont));

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

