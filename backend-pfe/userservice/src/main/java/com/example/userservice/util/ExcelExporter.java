package com.example.userservice.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExcelExporter {

    public static byte[] generateDashboardStatsExcel(List<String[]> rows) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Dashboard Summary");

            // Bold header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < rows.size(); i++) {
                Row row = sheet.createRow(i);
                String[] cols = rows.get(i);
                for (int j = 0; j < cols.length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(cols[j]);
                    if (i == 0) cell.setCellStyle(headerStyle); // apply to header
                }
            }
            // Add export timestamp as the last row
            int timestampRowIndex = rows.size();
            Row timestampRow = sheet.createRow(timestampRowIndex);
            timestampRow.createCell(0).setCellValue("Export Timestamp");
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            timestampRow.createCell(1).setCellValue(timestamp);

            // Auto-size columns
            for (int i = 0; i < rows.get(0).length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
