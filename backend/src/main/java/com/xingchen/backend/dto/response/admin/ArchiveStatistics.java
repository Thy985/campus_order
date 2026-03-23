package com.xingchen.backend.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 归档统计 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveStatistics {
    private Long totalOrders;
    private Long archivedOrders;
    private Long pendingArchiveOrders;
    private LocalDate lastArchiveDate;
}
