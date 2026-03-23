package com.xingchen.backend.dto.request.product;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新商品请求DTO
 */
@Data
public class UpdateProductRequest {

    @Size(max = 100, message = "商品名称最多100个字符")
    private String name;

    private String description;

    private String image;

    @Positive(message = "价格必须大于0")
    private BigDecimal price;

    private BigDecimal originalPrice;

    @Positive(message = "库存必须大于0")
    private Integer stock;

    private String category;

    private Integer status;
}
