package com.xingchen.backend.controller.merchant;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.xingchen.backend.entity.Product;
import com.xingchen.backend.service.MerchantProductService;
import com.xingchen.backend.dto.request.product.CreateProductRequest;
import com.xingchen.backend.dto.request.product.UpdateProductRequest;
import com.xingchen.backend.dto.response.product.ProductListResponse;
import com.xingchen.backend.util.Result;
import com.xingchen.backend.util.constant.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商家商品管理控制器
 *
 * @author 小跃
 * @date 2026-02-15
 */
@Slf4j
@RestController
@RequestMapping("/api/merchant/product")
@Tag(name = "商家商品管理", description = "商家商品相关接口")
@RequiredArgsConstructor
@SaCheckLogin
@SaCheckRole("merchant")
public class MerchantProductController {

    private final MerchantProductService merchantProductService;

    /**
     * 创建商品
     */
    @PostMapping("/create")
    @Operation(summary = "创建商品", description = "创建新商品")
    public Result<Product> createProduct(@Valid @RequestBody CreateProductRequest request) {
        Long merchantId = StpUtil.getLoginIdAsLong();
        Product product = merchantProductService.createProduct(request, merchantId);
        return Result.success("创建成功", product);
    }

    /**
     * 更新商品
     */
    @PutMapping("/update/{productId}")
    @Operation(summary = "更新商品", description = "更新商品信息")
    public Result<Product> updateProduct(
            @Parameter(description = "商品ID") @PathVariable Long productId,
            @Valid @RequestBody UpdateProductRequest request) {
        Long merchantId = StpUtil.getLoginIdAsLong();
        Product product = merchantProductService.updateProduct(productId, request, merchantId);
        return Result.success("更新成功", product);
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/delete/{productId}")
    @Operation(summary = "删除商品", description = "删除指定商品")
    public Result<Void> deleteProduct(
            @Parameter(description = "商品ID") @PathVariable Long productId) {
        Long merchantId = StpUtil.getLoginIdAsLong();
        merchantProductService.deleteProduct(productId, merchantId);
        return Result.success("删除成功", null);
    }

    /**
     * 商品上架
     */
    @PostMapping("/on-shelf/{productId}")
    @Operation(summary = "商品上架", description = "将商品上架")
    public Result<Void> onShelf(
            @Parameter(description = "商品ID") @PathVariable Long productId) {
        Long merchantId = StpUtil.getLoginIdAsLong();
        merchantProductService.updateProductStatus(productId, Constants.ProductStatus.ON_SHELF, merchantId);
        return Result.success("上架成功", null);
    }

    /**
     * 商品下架
     */
    @PostMapping("/off-shelf/{productId}")
    @Operation(summary = "商品下架", description = "将商品下架")
    public Result<Void> offShelf(
            @Parameter(description = "商品ID") @PathVariable Long productId) {
        Long merchantId = StpUtil.getLoginIdAsLong();
        merchantProductService.updateProductStatus(productId, Constants.ProductStatus.OFF_SHELF, merchantId);
        return Result.success("下架成功", null);
    }

    /**
     * 获取商品列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取商品列表", description = "获取当前商家的商品列表")
    public Result<ProductListResponse> getProductList(
            @Parameter(description = "状态：0-下架 1-上架，不传则全部") @RequestParam(required = false) Integer status,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        Long merchantId = StpUtil.getLoginIdAsLong();
        ProductListResponse response = merchantProductService.getMerchantProducts(merchantId, status, keyword, page, size);
        return Result.success("查询成功", response);
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/detail/{productId}")
    @Operation(summary = "获取商品详情", description = "获取指定商品的详细信息")
    public Result<Product> getProductDetail(
            @Parameter(description = "商品ID") @PathVariable Long productId) {
        Long merchantId = StpUtil.getLoginIdAsLong();
        Product product = merchantProductService.getProductDetail(productId, merchantId);
        return Result.success("查询成功", product);
    }

    /**
     * 批量更新库存
     */
    @PostMapping("/batch-update-stock")
    @Operation(summary = "批量更新库存", description = "批量更新商品库存")
    public Result<Void> batchUpdateStock(
            @RequestBody List<MerchantProductService.ProductStockUpdate> updates) {
        Long merchantId = StpUtil.getLoginIdAsLong();
        merchantProductService.batchUpdateStock(updates, merchantId);
        return Result.success("更新成功", null);
    }
}
