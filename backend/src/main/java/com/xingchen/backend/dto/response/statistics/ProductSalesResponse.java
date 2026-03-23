package com.xingchen.backend.dto.response.statistics;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品销售排行响应DTO
 */
@Data
public class ProductSalesResponse {

    private List<ProductSalesData> productList;

    @Data
    public static class ProductSalesData {
        private Long productId;
        private String productName;
        private Integer salesCount;
        private BigDecimal salesAmount;
    }
}
