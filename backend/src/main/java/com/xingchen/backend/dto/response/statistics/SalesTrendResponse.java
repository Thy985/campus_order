package com.xingchen.backend.dto.response.statistics;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 销售趋势响应DTO
 */
@Data
public class SalesTrendResponse {

    private List<DailyData> dataList;

    @Data
    public static class DailyData {
        private String date;
        private BigDecimal sales;
        private Integer orders;
    }
}
