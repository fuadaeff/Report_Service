package org.example.report_service.controller;

import lombok.RequiredArgsConstructor;
import org.example.report_service.dto.MonthlySalesReport;
import org.example.report_service.dto.TopProductReport;
import org.example.report_service.service.SalesReportService;
import org.example.report_service.service.TopProductsReportService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final SalesReportService salesReportService;
    private final TopProductsReportService topProductsReportService;

    @GetMapping("/monthly-sales")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MonthlySalesReport>> getMonthlySales(
            @RequestParam(defaultValue = "2025") int year) {
        return ResponseEntity.ok(salesReportService.getMonthlySales(year));
    }

    @GetMapping("/monthly-sales/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> exportMonthlySales(
            @RequestParam(defaultValue = "2025") int year) {
        String filePath = salesReportService.exportToExcel(year);
        Resource resource = new FileSystemResource(filePath);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=monthly-sales-" + year + ".xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    @GetMapping("/top-products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TopProductReport>> getTopProducts(
            @RequestParam(defaultValue = "2025") int year) {
        return ResponseEntity.ok(topProductsReportService.getTopProducts(year));
    }

    @GetMapping("/top-products/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> exportTopProducts(
            @RequestParam(defaultValue = "2025") int year) {
        String filePath = topProductsReportService.exportToExcel(year);
        Resource resource = new FileSystemResource(filePath);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=top-products-" + year + ".xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }
}