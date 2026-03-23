package com.xingchen.backend.dto.response.product;

import com.xingchen.backend.entity.Product;
import com.xingchen.backend.entity.ProductCategory;
import lombok.Data;

@Data
public class ProductDetailResponse {
    
    /**
     * 商品信息
     */
    private Product product;
    
    /**
     * 商品分类信息
     */
    private ProductCategory category;
}
