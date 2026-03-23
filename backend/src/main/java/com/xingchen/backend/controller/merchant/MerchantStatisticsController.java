package com.xingchen.backend.controller.merchant;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.xingchen.backend.entity.User;
import com.xingchen.backend.mapper.UserMapper;
import com.xingchen.backend.service.MerchantStatisticsService;
import com.xingchen.backend.util.Result;
import com.xingchen.backend.util.constant.ErrorCode;
import com.xingchen.backend.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商家统计控制器
 * 提供商家后台数据统计功能
 */
@Slf4j
@RestController
@RequestMapping("/api/merchant/statistics")
@Tag(name = "商家统计", description = "商家端数据统计接口")
@RequiredArgsConstructor
public class MerchantStatisticsController {

    private final MerchantStatisticsService statisticsService;
    private final UserMapper userMapper;

    /**
     * 获取当前登录用户的商家ID
     */
    private Long getCurrentMerchantId() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectOneById(userId);
        if (user == null || user.getMerchantId() == null) {
            throw new BusinessException(ErrorCode.USER_NO_PERMISSION, "您不是商家用户");
        }
        return user.getMerchantId();
    }

    /**
     * 获取商家统计数据
     */
    @SaCheckLogin
    @GetMapping
    @Operation(summary = "获取统计数据", description = "获取商家经营统计数据")
    public Result<Map<String, Object>> getStatistics() {
        Long merchantId = getCurrentMerchantId();

        // 调用服务获取真实统计数据
        Map<String, Object> stats = statisticsService.getStatistics(merchantId);

        return Result.success(stats);
    }

    /**
     * 获取销售统计
     */
    @SaCheckLogin
    @GetMapping("/sales")
    @Operation(summary = "获取销售统计", description = "获取商家销售统计数据")
    public Result<Map<String, Object>> getSalesStatistics() {
        Long merchantId = getCurrentMerchantId();

        Map<String, Object> stats = statisticsService.getStatistics(merchantId);

        return Result.success(stats);
    }

    /**
     * 获取商品销量统计
     */
    @SaCheckLogin
    @GetMapping("/products")
    @Operation(summary = "获取商品销量统计", description = "获取商家商品销量排行")
    public Result<List<Map<String, Object>>> getProductSales() {
        Long merchantId = getCurrentMerchantId();

        Map<String, Object> stats = statisticsService.getStatistics(merchantId);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topDishes = (List<Map<String, Object>>) stats.get("topDishes");

        return Result.success(topDishes);
    }

    /**
     * 获取营收趋势
     */
    @SaCheckLogin
    @GetMapping("/revenue")
    @Operation(summary = "获取营收趋势", description = "获取指定时间范围的营收趋势")
    public Result<List<Map<String, Object>>> getRevenueTrend(
            @RequestParam(value = "range", defaultValue = "week") String range) {
        Long merchantId = getCurrentMerchantId();

        List<Map<String, Object>> trend = statisticsService.getRevenueTrend(merchantId, range);

        return Result.success(trend);
    }

    /**
     * 获取热销菜品排行
     */
    @SaCheckLogin
    @GetMapping("/top-dishes")
    @Operation(summary = "获取热销菜品", description = "获取热销菜品排行")
    public Result<List<Map<String, Object>>> getTopDishes(
            @RequestParam(value = "limit", defaultValue = "5") Integer limit) {
        Long merchantId = getCurrentMerchantId();

        Map<String, Object> stats = statisticsService.getStatistics(merchantId);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topDishes = (List<Map<String, Object>>) stats.get("topDishes");

        if (topDishes != null && topDishes.size() > limit) {
            topDishes = topDishes.subList(0, limit);
        }

        return Result.success(topDishes);
    }
}
