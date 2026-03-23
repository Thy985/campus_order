package com.xingchen.backend.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.xingchen.backend.dto.response.admin.OrderDetailResponseDTO;
import com.xingchen.backend.dto.response.admin.OrderListResponseDTO;
import com.xingchen.backend.dto.response.admin.OrderStatisticsDTO;
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
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Tag(name = "Admin Order Management", description = "Admin order management endpoints")
public class AdminOrderController {

    private final AdminService adminService;

    @SaCheckLogin
    @SaCheckRole("admin")
    @GetMapping
    @Operation(summary = "Get order list", description = "Get all orders")
    public Result<OrderListResponseDTO> getOrderList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "merchantId", required = false) Long merchantId,
            @RequestParam(value = "userId", required = false) Long userId
    ) {
        OrderListResponseDTO result = adminService.getOrderList(page, pageSize, status, merchantId, userId);
        return Result.success(result);
    }

    @SaCheckLogin
    @SaCheckRole("admin")
    @GetMapping("/{orderId}")
    @Operation(summary = "Get order detail", description = "Get specified order details")
    public Result<OrderDetailResponseDTO> getOrderDetail(
            @Parameter(description = "Order ID") @PathVariable Long orderId
    ) {
        OrderDetailResponseDTO result = adminService.getOrderDetail(orderId);

        if (result == null) {
            return Result.error("Order does not exist");
        }

        return Result.success(result);
    }

    @SaCheckLogin
    @SaCheckRole("admin")
    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Admin cancel order")
    public Result<Void> cancelOrder(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @RequestParam(value = "reason", defaultValue = "Admin cancellation") String reason
    ) {
        log.info("Admin {} cancel order {}, reason: {}", StpUtil.getLoginId(), orderId, reason);

        boolean success = adminService.cancelOrder(orderId, reason);

        if (success) {
            return Result.success("Order cancelled", null);
        } else {
            return Result.error("Cancel failed");
        }
    }

    @SaCheckLogin
    @SaCheckRole("admin")
    @GetMapping("/statistics")
    @Operation(summary = "Get order statistics", description = "Get order statistics")
    public Result<OrderStatisticsDTO> getOrderStatistics() {
        OrderStatisticsDTO stats = adminService.getOrderStatistics();
        return Result.success(stats);
    }
}
