package org.example.report_service.repository;

import org.example.report_service.model.OrderSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SalesReportRepository extends JpaRepository<OrderSnapshot, Long> {

    @Query("SELECT o FROM OrderSnapshot o WHERE o.createdAt BETWEEN :start AND :end AND o.status = 'CONFIRMED'")
    List<OrderSnapshot> findConfirmedOrdersBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}