package com.xingchen.backend.controller.merchant;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.xingchen.backend.service.MerchantService;
import com.xingchen.backend.service.ProductService;
import com.xingchen.backend.service.ProductCategoryService;
import com.xingchen.backend.dto.request.merchant.MerchantListRequest;
import com.xingchen.backend.dto.request.product.ProductListRequest;
import com.xingchen.backend.dto.response.merchant.MerchantDetailResponse;
import com.xingchen.backend.dto.response.merchant.MerchantListResponse;
import com.xingchen.backend.dto.response.product.ProductListResponse;
import com.xingchen.backend.entity.Merchant;
import com.xingchen.backend.entity.MerchantCategory;
import com.xingchen.backend.entity.ProductCategory;
import com.xingchen.backend.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
public class MerchantController {
    
    private final MerchantService merchantService;
    private final ProductService productService;
    private final ProductCategoryService productCategoryService;
    
    @GetMapping("/list")
    public Result<MerchantListResponse> getMerchantList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status
    ) {
        // 参数校验：page必须大于等于1
        if (page == null || page < 1) {
            return Result.badRequest("页码必须大于等于1");
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            return Result.badRequest("每页数量必须在1-100之间");
        }
        
        MerchantListRequest request = new MerchantListRequest();
        request.setPage(page);
        request.setPageSize(pageSize);
        request.setCategoryId(categoryId);
        request.setKeyword(keyword);

        MerchantListResponse response = merchantService.getMerchantList(request);
        return Result.success(response);
    }

    @GetMapping("/detail/{id}")
    public Result<MerchantDetailResponse> getMerchantDetail(@PathVariable String id) {
        // 参数验证：ID必须是有效的数字
        Long merchantId;
        try {
            merchantId = Long.parseLong(id);
            if (merchantId <= 0) {
                return Result.badRequest("商家ID必须大于0");
            }
        } catch (NumberFormatException e) {
            return Result.badRequest("商家ID格式不正确");
        }
        
        MerchantDetailResponse response = merchantService.getMerchantDetail(merchantId);
        if (response == null) {
            return Result.notFound("商家不存在");
        }
        return Result.success(response);
    }
    
    @GetMapping("/category/list")
    public Result<List<MerchantCategory>> getMerchantCategoryList() {
        List<MerchantCategory> categoryList = merchantService.getMerchantCategoryList();
        return Result.success(categoryList);
    }
    
    @SaCheckLogin
    @PostMapping
    public Result<Merchant> createMerchant(@RequestBody Merchant merchant) {
        if (merchant.getName() == null || merchant.getName().trim().isEmpty()) {
            return Result.badRequest("商家名称不能为空");
        }
        
        Merchant createdMerchant = merchantService.createMerchant(merchant);
        return Result.success(createdMerchant);
    }
    
    @SaCheckLogin
    @PutMapping("/{id}")
    public Result<Merchant> updateMerchant(@PathVariable Long id, @RequestBody Merchant merchant) {
        Merchant existingMerchant = merchantService.getMerchantById(id);
        if (existingMerchant == null) {
            return Result.notFound("商家不存在");
        }

        merchant.setId(id);
        Merchant updatedMerchant = merchantService.updateMerchant(merchant);
        return Result.success(updatedMerchant);
    }

    @SaCheckLogin
    @DeleteMapping("/{id}")
    public Result<Void> deleteMerchant(@PathVariable Long id) {
        Merchant existingMerchant = merchantService.getMerchantById(id);
        if (existingMerchant == null) {
            return Result.notFound("商家不存在");
        }
        
        merchantService.deleteMerchant(id);
        return Result.success("商家删除成功");
    }
    
    @SaCheckLogin
    @PutMapping("/{id}/status")
    public Result<Void> updateMerchantStatus(@PathVariable Long id, @RequestParam Integer status) {
        Merchant existingMerchant = merchantService.getMerchantById(id);
        if (existingMerchant == null) {
            return Result.notFound("商家不存在");
        }
        
        existingMerchant.setStatus(status);
        merchantService.updateMerchant(existingMerchant);
        return Result.success("商家状态更新成功");
    }
    
    @GetMapping("/search")
    public Result<MerchantListResponse> searchMerchants(
            @RequestParam String keyword,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        MerchantListRequest request = new MerchantListRequest();
        request.setPage(page);
        request.setPageSize(pageSize);
        request.setKeyword(keyword);

        MerchantListResponse response = merchantService.getMerchantList(request);
        return Result.success(response);
    }
    
    @GetMapping("/nearby")
    public Result<MerchantListResponse> getNearbyMerchants(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(value = "distance", defaultValue = "5000") Integer distance,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        MerchantListRequest request = new MerchantListRequest();
        request.setPage(page);
        request.setPageSize(pageSize);
        
        MerchantListResponse response = merchantService.getMerchantList(request);
        return Result.success(response);
    }
    
    @GetMapping("/hot")
    public Result<List<Merchant>> getHotMerchants(
            @RequestParam(value = "limit", defaultValue = "10") Integer limit
    ) {
        MerchantListRequest request = new MerchantListRequest();
        request.setPage(1);
        request.setPageSize(limit);
        
        MerchantListResponse response = merchantService.getMerchantList(request);
        List<Merchant> list = response.getMerchantList();
        return Result.success(list != null ? list : new java.util.ArrayList<>());
    }
    
    @GetMapping("/products")
    public Result<ProductListResponse> getProducts(
            @RequestParam Long merchantId,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        ProductListRequest request = new ProductListRequest();
        request.setMerchantId(merchantId);
        request.setCategoryId(categoryId);
        request.setPage(page);
        request.setPageSize(pageSize);
        
        ProductListResponse response = productService.getProductList(request);
        return Result.success(response);
    }
    
    @GetMapping("/products/categories")
    public Result<List<ProductCategory>> getProductCategories(
            @RequestParam(required = false) Long merchantId
    ) {
        List<ProductCategory> categories = productCategoryService.getProductCategoryList();
        return Result.success(categories);
    }
}
