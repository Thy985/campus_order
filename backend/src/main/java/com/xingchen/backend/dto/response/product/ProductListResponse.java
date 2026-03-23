package com.xingchen.backend.dto.response.product;

import com.xingchen.backend.entity.Product;
import lombok.Data;

import java.util.List;

/**
 * 商品列表响应DTO
 */
@Data
public class ProductListResponse {

    private List<Product> productList;

    private Integer page;

    private Integer pageSize;

    private Long total;

    private Integer totalPages;
}
