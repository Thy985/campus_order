package com.xingchen.backend.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘概览 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewDTO {
    private Long totalUsers;
    private Long totalMerchants;
    private Long totalProducts;
    private Long totalOrders;
    private Long todayOrders;
    private Long pendingOrders;
    private BigDecimal totalRevenue;
}
