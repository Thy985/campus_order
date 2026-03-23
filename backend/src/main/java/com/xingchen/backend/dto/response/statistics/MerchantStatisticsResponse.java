package com.xingchen.backend.dto.response.statistics;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商家统计响应DTO
 */
@Data
public class MerchantStatisticsResponse {

    // 总销售额
    private BigDecimal totalSales;

    // 总订单数
    private Integer totalOrders;

    // 今日数据
    private TodayData today;

    // 本月数据
    private MonthData month;

    @Data
    public static class TodayData {
        private BigDecimal sales;
        private Integer orders;
        private Integer visitors;
    }

    @Data
    public static class MonthData {
        private BigDecimal sales;
        private Integer orders;
        private BigDecimal avgOrderAmount;
    }
}
