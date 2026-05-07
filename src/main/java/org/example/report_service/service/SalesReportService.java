package org.example.report_service.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.report_service.dto.MonthlySalesReport;
import org.example.report_service.model.OrderSnapshot;
import org.example.report_service.repository.SalesReportRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesReportService {

    private final SalesReportRepository salesReportRepository;

    @Value("${report.storage.path}")
    private String storagePath;

    public List<MonthlySalesReport> getMonthlySales(int year) {
        LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(year, 12, 31, 23, 59, 59);

        List<OrderSnapshot> orders = salesReportRepository.findConfirmedOrdersBetween(start, end);

        Map<Integer, List<OrderSnapshot>> byMonth = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getCreatedAt().getMonthValue()));

        return byMonth.entrySet().stream().map(entry -> {
                    int month = entry.getKey();
                    List<OrderSnapshot> monthOrders = entry.getValue();
                    BigDecimal revenue = monthOrders.stream()
                            .map(OrderSnapshot::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return MonthlySalesReport.builder()
                            .year(year)
                            .month(month)
                            .totalOrders(monthOrders.size())
                            .totalRevenue(revenue)
                            .build();
                }).sorted((a, b) -> Integer.compare(a.getMonth(), b.getMonth()))
                .collect(Collectors.toList());
    }

    public String exportToExcel(int year) {
        List<MonthlySalesReport> reports = getMonthlySales(year);

        new File(storagePath).mkdirs();
        String filePath = storagePath + "monthly-sales-" + year + ".xlsx";

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Monthly Sales " + year);
            Row header = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            String[] columns = {"Year", "Month", "Total Orders", "Total Revenue"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (MonthlySalesReport r : reports) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(r.getYear());
                row.createCell(1).setCellValue(r.getMonth());
                row.createCell(2).setCellValue(r.getTotalOrders());
                row.createCell(3).setCellValue(r.getTotalRevenue().doubleValue());
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
