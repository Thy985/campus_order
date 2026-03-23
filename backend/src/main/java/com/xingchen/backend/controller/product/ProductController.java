package com.xingchen.backend.controller.product;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.xingchen.backend.service.ProductService;
import com.xingchen.backend.entity.Product;
import com.xingchen.backend.dto.request.product.ProductListRequest;
import com.xingchen.backend.dto.response.product.ProductDetailResponse;
import com.xingchen.backend.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    
    @GetMapping("/list")
    public Result<?> getProductList(
            @RequestParam Long merchantId,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice
    ) {
        ProductListRequest request = new ProductListRequest();
        request.setMerchantId(merchantId);
        request.setCategoryId(categoryId);
        request.setPage(page);
        request.setPageSize(pageSize);
        
        return Result.success(productService.getProductList(request));
    }
    
    @GetMapping("/detail/{id}")
    public Result<ProductDetailResponse> getProductDetail(@PathVariable String id) {
        // 参数验证：ID必须是有效的数字
        Long productId;
        try {
            productId = Long.parseLong(id);
            if (productId <= 0) {
                return Result.badRequest("商品ID必须大于0");
            }
        } catch (NumberFormatException e) {
            return Result.badRequest("商品ID格式不正确");
        }
        
        ProductDetailResponse response = productService.getProductDetail(productId);
        if (response == null) {
            return Result.notFound("商品不存在");
        }
        return Result.success(response);
    }
    
    @SaCheckLogin
    @PostMapping
    public Result<Product> createProduct(@RequestBody Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            return Result.badRequest("商品名称不能为空");
        }
        if (product.getMerchantId() == null) {
            return Result.badRequest("商家ID不能为空");
        }
        if (product.getPrice() == null) {
            return Result.badRequest("商品价格不能为空");
        }
        
        Product createdProduct = productService.createProduct(product);
        return Result.success(createdProduct);
    }
    
    @SaCheckLogin
    @PutMapping("/{id}")
    public Result<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        Product existingProduct = productService.getProductById(id);
        if (existingProduct == null) {
            return Result.notFound("商品不存在");
        }
        
        product.setId(id);
        Product updatedProduct = productService.updateProduct(product);
        return Result.success(updatedProduct);
    }
    
    @SaCheckLogin
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        Product existingProduct = productService.getProductById(id);
        if (existingProduct == null) {
            return Result.notFound("商品不存在");
        }
        
        productService.deleteProduct(id);
        return Result.success("商品删除成功");
    }
    
    @SaCheckLogin
    @PutMapping("/{id}/stock")
    public Result<Void> updateStock(@PathVariable Long id, @RequestParam Integer stock) {
        Product existingProduct = productService.getProductById(id);
        if (existingProduct == null) {
            return Result.notFound("商品不存在");
        }
        
        if (stock < 0) {
            return Result.badRequest("库存不能为负数");
        }
        
        productService.updateStock(id, stock);
        return Result.success("库存更新成功");
    }
    
    @SaCheckLogin
    @PutMapping("/{id}/status")
    public Result<Void> updateProductStatus(@PathVariable Long id, @RequestParam Integer status) {
        Product existingProduct = productService.getProductById(id);
        if (existingProduct == null) {
            return Result.notFound("商品不存在");
        }
        
        existingProduct.setStatus(status);
        productService.updateProduct(existingProduct);
        return Result.success("商品状态更新成功");
    }
    
    @GetMapping("/merchant/{merchantId}")
    public Result<List<Product>> getProductsByMerchantId(@PathVariable Long merchantId) {
        List<Product> products = productService.getProductsByMerchantId(merchantId);
        return Result.success(products);
    }
    
    @GetMapping("/search")
    public Result<?> searchProducts(
            @RequestParam String keyword,
            @RequestParam(value = "merchantId", required = false) Long merchantId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        ProductListRequest request = new ProductListRequest();
        request.setMerchantId(merchantId);
        request.setKeyword(keyword);
        request.setPage(page);
        request.setPageSize(pageSize);

        return Result.success(productService.searchProducts(request));
    }
    
    @GetMapping("/hot")
    public Result<?> getHotProducts(
            @RequestParam Long merchantId,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit
    ) {
        ProductListRequest request = new ProductListRequest();
        request.setMerchantId(merchantId);
        request.setPage(1);
        request.setPageSize(limit);
        
        return Result.success(productService.getProductList(request));
    }
    
    @SaCheckLogin
    @PostMapping("/batch")
    public Result<Void> batchCreateProducts(@RequestBody List<Product> products) {
        if (products == null || products.isEmpty()) {
            return Result.badRequest("商品列表不能为空");
        }
        
        for (Product product : products) {
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                return Result.badRequest("商品名称不能为空");
            }
            if (product.getMerchantId() == null) {
                return Result.badRequest("商家ID不能为空");
            }
            productService.createProduct(product);
        }
        
        return Result.success("批量创建成功");
    }
}
