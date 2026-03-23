package com.xingchen.backend.service;

import com.xingchen.backend.entity.Product;
import com.xingchen.backend.dto.request.product.ProductListRequest;
import com.xingchen.backend.dto.response.product.ProductDetailResponse;
import com.xingchen.backend.dto.response.product.ProductListResponse;

import java.util.List;

public interface ProductService {
    
    /**
     * 获取商品列表
     */
    ProductListResponse getProductList(ProductListRequest request);
    
    /**
     * 获取商品详情
     */
    ProductDetailResponse getProductDetail(Long id);
    
    /**
     * 创建商品
     */
    Product createProduct(Product product);
    
    /**
     * 更新商品
     */
    Product updateProduct(Product product);
    
    /**
     * 删除商品
     */
    void deleteProduct(Long id);
    
    /**
     * 更新商品库存
     */
    void updateStock(Long productId, Integer stock);
    
    /**
     * 减少商品库存
     */
    boolean decreaseStock(Long productId, Integer quantity);
    
    /**
     * 增加商品库存
     */
    void increaseStock(Long productId, Integer quantity);
    
    /**
     * 根据ID获取商品
     */
    Product getProductById(Long id);
    
    /**
     * 根据商家ID获取商品列表
     */
    List<Product> getProductsByMerchantId(Long merchantId);

    /**
     * 搜索商品
     */
    ProductListResponse searchProducts(ProductListRequest request);
}
