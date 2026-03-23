package com.xingchen.backend.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单统计 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatisticsDTO {
    private Long totalOrders;
    private Long todayOrders;
    private Long pendingOrders;
    private BigDecimal totalRevenue;
}
