package org.example.report_service.repository;

import org.example.report_service.model.OrderItemSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductRevenueRepository extends JpaRepository<OrderItemSnapshot, Long> {

    @Query("""
            SELECT i.productId, SUM(i.quantity) as totalQty, SUM(i.quantity * i.unitPrice) as totalRevenue
            FROM OrderItemSnapshot i
            WHERE i.order.createdAt BETWEEN :start AND :end
            AND i.order.status = 'CONFIRMED'
            GROUP BY i.productId
            ORDER BY totalRevenue DESC
            """)
    List<Object[]> findTopProducts(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}

