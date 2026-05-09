package org.example.report_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.example.report_service.dto.TopProductReport;
import org.example.report_service.repository.ProductRevenueRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopProductsReportService {

    private final ProductRevenueRepository productRevenueRepository;
    private final ExcelExportService excelExportService;

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

        List<String> headers = List.of("Product ID", "Total Quantity", "Total Revenue");

        List<List<String>> rows = reports.stream().map(r -> List.of(
                String.valueOf(r.getProductId()),
                String.valueOf(r.getTotalQuantity()),
                r.getTotalRevenue().toString()
        )).collect(Collectors.toList());


        return excelExportService.export(
                "Top Products " + year,
                "top-products-" + year + ".xlsx",
                headers,
                rows
        );
    }
}
