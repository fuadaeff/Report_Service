package org.example.report_service.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.example.report_service.dto.MonthlySalesReport;
import org.example.report_service.model.OrderSnapshot;
import org.example.report_service.repository.SalesReportRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
    private final ExcelExportService excelExportService;

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

        List<String> headers = List.of("Year", "Month", "Total Orders", "Total Revenue");

        List<List<String>> rows = reports.stream().map(r -> List.of(
                String.valueOf(r.getYear()),
                String.valueOf(r.getMonth()),
                String.valueOf(r.getTotalOrders()),
                r.getTotalRevenue().toString()
        )).collect(Collectors.toList());

        return excelExportService.export(
                "Monthly Sales " + year,
                "monthly-sales-" + year + ".xlsx",
                headers,
                rows
        );
    }
}
