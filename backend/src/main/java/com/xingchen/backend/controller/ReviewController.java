package com.xingchen.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.xingchen.backend.entity.Review;
import com.xingchen.backend.service.ReviewService;
import com.xingchen.backend.dto.request.review.CreateReviewRequest;
import com.xingchen.backend.dto.request.review.ReplyReviewRequest;
import com.xingchen.backend.dto.response.review.ReviewListResponse;
import com.xingchen.backend.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 评价控制器
 * @author 小跃
 * @date 2026-02-15
 */
@Slf4j
@RestController
@RequestMapping("/api/review")
@Tag(name = "评价管理", description = "订单评价相关接口")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 创建评价
     */
    @SaCheckLogin
    @PostMapping("/create")
    @Operation(summary = "创建评价", description = "对已完成订单进行评价")
    public Result<Review> createReview(@Valid @RequestBody CreateReviewRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        Review review = reviewService.createReview(request, userId);
        return Result.success("评价成功", review);
    }

    /**
     * 获取商家的评价列表
     */
    @GetMapping("/merchant/{merchantId}")
    @Operation(summary = "获取商家评价", description = "获取指定商家的评价列表")
    public Result<ReviewListResponse> getMerchantReviews(
            @Parameter(description = "商家ID") @PathVariable Long merchantId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        ReviewListResponse response = reviewService.getMerchantReviews(merchantId, page, size);
        return Result.success("查询成功", response);
    }

    /**
     * 获取订单的评价
     */
    @GetMapping("/order/{orderId}")
    @Operation(summary = "获取订单评价", description = "获取指定订单的评价信息")
    public Result<Review> getReviewByOrderId(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        Review review = reviewService.getReviewByOrderId(orderId);
        return Result.success("查询成功", review);
    }

    /**
     * 检查订单是否已评价
     */
    @SaCheckLogin
    @GetMapping("/has-reviewed/{orderId}")
    @Operation(summary = "检查是否已评价", description = "检查订单是否已被当前用户评价")
    public Result<Boolean> hasReviewed(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        boolean hasReviewed = reviewService.hasReviewed(orderId);
        return Result.success("查询成功", hasReviewed);
    }

    /**
     * 商家回复评价
     */
    @SaCheckLogin
    @PostMapping("/reply")
    @Operation(summary = "回复评价", description = "商家回复用户评价")
    public Result<Void> replyReview(@Valid @RequestBody ReplyReviewRequest request) {
        Long merchantId = StpUtil.getLoginIdAsLong();
        reviewService.replyReview(request, merchantId);
        return Result.success("回复成功", null);
    }
}
