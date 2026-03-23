package com.xingchen.backend.service;

import com.xingchen.backend.entity.Product;
import com.xingchen.backend.dto.request.product.CreateProductRequest;
import com.xingchen.backend.dto.request.product.UpdateProductRequest;
import com.xingchen.backend.dto.response.product.ProductListResponse;

import java.util.List;

/**
 * 商家商品服务接口
 */
public interface MerchantProductService {

    /**
     * 创建商品
     */
    Product createProduct(CreateProductRequest request, Long merchantId);

    /**
     * 更新商品
     */
    Product updateProduct(Long productId, UpdateProductRequest request, Long merchantId);

    /**
     * 删除商品（逻辑删除?     */
    void deleteProduct(Long productId, Long merchantId);

    /**
     * 商品上架/下架
     */
    void updateProductStatus(Long productId, Integer status, Long merchantId);

    /**
     * 获取商家的商品列?     */
    ProductListResponse getMerchantProducts(Long merchantId, Integer status, String keyword, int page, int size);

    /**
     * 获取商品详情
     */
    Product getProductDetail(Long productId, Long merchantId);

    /**
     * 批量更新库存
     */
    void batchUpdateStock(List<ProductStockUpdate> updates, Long merchantId);

    /**
     * 商品库存更新DTO
     */
    class ProductStockUpdate {
        private Long productId;
        private Integer stock;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Integer getStock() { return stock; }
        public void setStock(Integer stock) { this.stock = stock; }
    }
}
