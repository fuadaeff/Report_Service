package org.example.report_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySalesReport {
    private int year;
    private int month;
    private long totalOrders;
    private BigDecimal totalRevenue;
}
