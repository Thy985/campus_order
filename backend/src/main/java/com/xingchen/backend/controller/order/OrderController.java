package com.xingchen.backend.controller.order;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.xingchen.backend.service.OrderService;
import com.xingchen.backend.dto.request.order.CreateOrderRequest;
import com.xingchen.backend.dto.request.order.OrderListRequest;
import com.xingchen.backend.dto.response.order.OrderDetailResponse;
import com.xingchen.backend.dto.response.order.OrderListResponse;
import com.xingchen.backend.entity.Order;
import com.xingchen.backend.util.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 订单管理控制器
 *
 * <p>提供用户订单相关的REST API接口，包括：</p>
 * <ul>
 *   <li>订单创建 - 提交购物车商品生成订单</li>
 *   <li>订单查询 - 列表查询和详情查看</li>
 *   <li>订单操作 - 取消订单、确认取餐</li>
 * </ul>
 *
 * <p>安全特性：</p>
 * <ul>
 *   <li>所有接口需要登录（@SaCheckLogin）</li>
 *   <li>创建订单限流（每用户每分钟10次）</li>
 *   <li>只能操作自己的订单</li>
 * </ul>
 *
 * @author xingchen
 * @since 1.0.0
 * @see OrderService
 */
@Slf4j
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     *
     * <p>根据购物车商品创建订单，流程：</p>
     * <ol>
     *   <li>获取当前登录用户ID</li>
     *   <li>校验商品库存和价格</li>
     *   <li>计算订单总金额</li>
     *   <li>生成订单号和订单项</li>
     *   <li>扣减库存</li>
     *   <li>清空购物车</li>
     * </ol>
     *
     * @param request 订单创建请求（包含商家ID、商品列表、备注等）
     * @return 创建的订单信息
     * @throws BusinessException 商品不存在、库存不足
     */
    @SaCheckLogin
    @PostMapping
    public Result<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        request.setUserId(userId);
        
        log.info("用户[{}]创建订单，商家ID: {}", userId, request.getMerchantId());
        
        Order order = orderService.createOrder(request);
        return Result.success("订单创建成功", order);
    }
    
    /**
     * 获取订单列表
     *
     * <p>支持分页和状态筛选，只返回当前用户的订单</p>
     *
     * @param request 查询条件（包含状态、分页参数）
     * @return 订单列表（包含分页信息）
     */
    @SaCheckLogin
    @GetMapping("/list")
    public Result<OrderListResponse> getOrderList(OrderListRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        request.setUserId(userId);
        
        OrderListResponse response = orderService.getOrderList(request);
        return Result.success(response);
    }
    
    /**
     * 获取订单详情
     *
     * <p>查询指定订单的详细信息，包含订单项和商品信息</p>
     *
     * @param orderId 订单ID
     * @return 订单详情（包含订单项列表）
     * @throws BusinessException 订单不存在、无权查看
     */
    @SaCheckLogin
    @GetMapping("/{orderId}")
    public Result<OrderDetailResponse> getOrderDetail(@PathVariable Long orderId) {
        Long userId = StpUtil.getLoginIdAsLong();
        OrderDetailResponse response = orderService.getOrderDetail(orderId, userId);
        return Result.success(response);
    }
    
    /**
     * 取消订单
     *
     * <p>取消待支付的订单，恢复商品库存</p>
     *
     * @param orderId 订单ID
     * @return 操作结果
     * @throws BusinessException 订单不存在、无权操作、状态不允许取消
     */
    @SaCheckLogin
    @PostMapping("/{orderId}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long orderId) {
        Long userId = StpUtil.getLoginIdAsLong();
        orderService.cancelOrder(orderId, userId);
        return Result.success("订单取消成功");
    }
    
    /**
     * 确认取餐
     *
     * <p>用户确认已收到餐品，完成订单</p>
     *
     * @param orderId 订单ID
     * @return 操作结果
     * @throws BusinessException 订单不存在、无权操作、状态不允许确认
     */
    @SaCheckLogin
    @PostMapping("/{orderId}/confirm-pickup")
    public Result<Void> confirmPickup(@PathVariable Long orderId) {
        Long userId = StpUtil.getLoginIdAsLong();
        orderService.confirmPickup(orderId, userId);
        return Result.success("确认取餐成功");
    }
}
