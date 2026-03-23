package com.xingchen.backend.controller.merchant;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.xingchen.backend.entity.Order;
import com.xingchen.backend.entity.User;
import com.xingchen.backend.mapper.OrderMapper;
import com.xingchen.backend.mapper.UserMapper;
import com.xingchen.backend.service.OrderService;
import com.xingchen.backend.service.OrderQueryService;
import com.xingchen.backend.dto.response.order.OrderDetailDTO;
import com.xingchen.backend.util.Result;
import com.xingchen.backend.util.constant.ErrorCode;
import com.xingchen.backend.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商家订单管理控制器
 * 提供商家订单查询、接单、拒单等功能
 */
@Slf4j
@RestController
@RequestMapping("/api/merchant/orders")
@Tag(name = "商家订单", description = "商家端订单管理接口")
@RequiredArgsConstructor
@SaCheckLogin
@SaCheckRole("merchant")
public class MerchantOrderController {

    private final OrderService orderService;
    private final OrderQueryService orderQueryService;
    private final OrderMapper orderMapper;
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
     * 获取商家订单列表
     */
    @GetMapping
    @Operation(summary = "获取订单列表", description = "获取当前商家的订单列表")
    public Result<Map<String, Object>> getMerchantOrders(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "status", required = false) Integer status
    ) {
        Long merchantId = getCurrentMerchantId();

        // 查询商家的订单列表
        List<Order> orders;
        long total;
        
        if (status != null) {
            orders = orderMapper.selectByMerchantIdAndStatus(
                    merchantId, status, pageSize, (page - 1) * pageSize);
            total = orderMapper.countByMerchantIdAndStatus(merchantId, status);
        } else {
            orders = orderMapper.selectByMerchantId(
                    merchantId, pageSize, (page - 1) * pageSize);
            total = orderMapper.countByMerchantId(merchantId);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("orderList", orders);
        
        return Result.success(result);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "获取订单详情", description = "获取指定订单的详细信息")
    public Result<OrderDetailDTO> getOrderDetail(
            @Parameter(description = "订单ID") @PathVariable Long orderId
    ) {
        Long merchantId = getCurrentMerchantId();

        OrderDetailDTO detail = orderQueryService.getMerchantOrderDetail(orderId, merchantId);

        if (detail == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXIST, "订单不存在或无权限查看");
        }

        return Result.success(detail);
    }

    /**
     * 接单
     */
    @PostMapping("/{orderId}/accept")
    @Operation(summary = "接单", description = "商家接受订单")
    public Result<Void> acceptOrder(
            @Parameter(description = "订单ID") @PathVariable Long orderId
    ) {
        Long merchantId = getCurrentMerchantId();

        // 验证订单是否属于该商家
        Order order = orderMapper.selectOneById(orderId);
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXIST, "订单不存在");
        }
        
        // 更新订单状态为制作中
        order.setStatus(3); // MAKING
        orderMapper.update(order);
        
        log.info("商家 {} 接单 {}", merchantId, orderId);
        
        return Result.success("接单成功", null);
    }

    /**
     * 拒单
     */
    @PostMapping("/{orderId}/reject")
    @Operation(summary = "拒单", description = "商家拒绝订单")
    public Result<Void> rejectOrder(
            @Parameter(description = "订单ID") @PathVariable Long orderId,
            @RequestParam(value = "reason", defaultValue = "商家拒单") String reason
    ) {
        Long merchantId = getCurrentMerchantId();

        // 验证订单是否属于该商家
        Order order = orderMapper.selectOneById(orderId);
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXIST, "订单不存在");
        }
        
        // 更新订单状态为已拒绝
        order.setStatus(7); // REFUSED
        order.setRemark(reason);
        orderMapper.update(order);
        
        log.info("商家 {} 拒单 {}，原因：{}", merchantId, orderId, reason);
        
        return Result.success("拒单成功", null);
    }

    /**
     * 完成订单
     */
    @PostMapping("/{orderId}/complete")
    @Operation(summary = "完成订单", description = "商家完成订单制作")
    public Result<Void> completeOrder(
            @Parameter(description = "订单ID") @PathVariable Long orderId
    ) {
        Long merchantId = getCurrentMerchantId();

        // 验证订单是否属于该商家
        Order order = orderMapper.selectOneById(orderId);
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXIST, "订单不存在");
        }
        
        // 更新订单状态为待取餐
        order.setStatus(4); // WAIT_PICKUP
        orderMapper.update(order);
        
        log.info("商家 {} 完成订单 {}", merchantId, orderId);
        
        return Result.success("订单已完成，等待用户取餐", null);
    }

    /**
     * 获取待处理订单数量
     */
    @GetMapping("/pending-count")
    @Operation(summary = "获取待处理数量", description = "获取待处理订单数量")
    public Result<Map<String, Integer>> getPendingCount() {
        Long merchantId = getCurrentMerchantId();
        
        // 查询待接单数量（状态2）
        long pendingAcceptance = orderMapper.countByMerchantIdAndStatus(
                merchantId, 2);
        
        // 查询制作中数量（状态3）
        long preparing = orderMapper.countByMerchantIdAndStatus(
                merchantId, 3);
        
        Map<String, Integer> count = new HashMap<>();
        count.put("pendingAcceptance", (int) pendingAcceptance);
        count.put("preparing", (int) preparing);
        count.put("total", (int) (pendingAcceptance + preparing));
        
        return Result.success(count);
    }
}
