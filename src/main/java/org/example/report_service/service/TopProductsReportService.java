package org.example.report_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.report_service.dto.TopProductReport;
import org.example.report_service.repository.ProductRevenueRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopProductsReportService {

    private final ProductRevenueRepository productRevenueRepository;

    @Value("${report.storage.path}")
    private String storagePath;

    public List<TopProductReport> getTopProducts(int year) {
        LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(year, 12, 31, 23, 59, 59);

        List<Object[]> results = productRevenueRepository.findTopProducts(start, end);

        return results.stream().limit(10).map(row -> TopProductReport.builder()
                .productId(((Number) row[0]).longValue())
                .totalQuantity(((Number) row[1]).longValue())
                .totalRevenue(new BigDecimal(row[2].toString()))
                .build()
        ).collect(Collectors.toList());
    }

    public String exportToExcel(int year) {
        List<TopProductReport> reports = getTopProducts(year);

        new File(storagePath).mkdirs();
        String filePath = storagePath + "top-products-" + year + ".xlsx";

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Top Products " + year);
            Row header = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            String[] columns = {"Product ID", "Total Quantity", "Total Revenue"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (TopProductReport r : reports) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(r.getProductId());
                row.createCell(1).setCellValue(r.getTotalQuantity());
                row.createCell(2).setCellValue(r.getTotalRevenue().doubleValue());
            }

            for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to export Excel: " + e.getMessage());
        }

        return filePath;
    }
}
