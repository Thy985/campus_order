package com.xingchen.backend.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.xingchen.backend.dto.response.admin.DashboardOverviewDTO;
import com.xingchen.backend.dto.response.admin.OrderStatisticsDTO;
import com.xingchen.backend.dto.response.admin.OrderTrendDTO;
import com.xingchen.backend.dto.response.admin.RecentOrdersDTO;
import com.xingchen.backend.service.AdminService;
import com.xingchen.backend.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard Statistics", description = "Admin dashboard data endpoints")
@SaCheckRole("admin")
public class DashboardController {

    private final AdminService adminService;

    @GetMapping("/overview")
    @Operation(summary = "Get overview data", description = "Get dashboard overview data")
    public Result<DashboardOverviewDTO> getOverview() {
        DashboardOverviewDTO overview = adminService.getDashboardOverview();
        return Result.success(overview);
    }

    @GetMapping("/order-trend")
    @Operation(summary = "Get order trend", description = "Get order trend data for recent days")
    public Result<OrderTrendDTO> getOrderTrend(
            @Parameter(description = "Days") @RequestParam(value = "days", defaultValue = "7") Integer days) {
        OrderTrendDTO trend = adminService.getOrderTrend(days);
        return Result.success(trend);
    }

    @GetMapping("/recent-orders")
    @Operation(summary = "Get recent orders", description = "Get recent orders list")
    public Result<RecentOrdersDTO> getRecentOrders(
            @Parameter(description = "Limit") @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        RecentOrdersDTO orders = adminService.getRecentOrders(limit);
        return Result.success(orders);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get order statistics", description = "Get order statistics data")
    public Result<OrderStatisticsDTO> getStatistics() {
        OrderStatisticsDTO stats = adminService.getOrderStatistics();
        return Result.success(stats);
    }
}
