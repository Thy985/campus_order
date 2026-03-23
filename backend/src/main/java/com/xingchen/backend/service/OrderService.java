package com.xingchen.backend.service;

import com.xingchen.backend.entity.Order;
import com.xingchen.backend.entity.OrderItem;
import com.xingchen.backend.dto.request.order.CreateOrderRequest;
import com.xingchen.backend.dto.request.order.OrderListRequest;
import com.xingchen.backend.dto.response.order.OrderDetailResponse;
import com.xingchen.backend.dto.response.order.OrderListResponse;

import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 创建订单
     */
    Order createOrder(CreateOrderRequest request);

    /**
     * 获取订单列表
     */
    OrderListResponse getOrderList(OrderListRequest request);

    /**
     * 获取用户订单列表
     *
     * @param userId  用户ID
     * @param request 查询条件
     * @return 订单列表响应
     */
    OrderListResponse getUserOrders(Long userId, OrderListRequest request);

    /**
     * 获取订单详情
     *
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 订单详情
     */
    OrderDetailResponse getOrderDetail(Long orderId, Long userId);

    /**
     * 更新订单状?     */
    void updateOrderStatus(Long orderId, Integer status);

    /**
     * 更新订单支付状?     */
    void updatePaymentStatus(Long orderId, Integer paymentStatus);

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @param userId 用户ID
     */
    void cancelOrder(Long orderId, Long userId);

    /**
     * 用户确认收货
     *
     * @param orderId 订单ID
     * @param userId 用户ID
     */
    void confirmOrder(Long orderId, Long userId);

    /**
     * 用户确认取餐（校园点餐场景）
     *
     * @param orderId 订单ID
     * @param userId 用户ID
     */
    void confirmPickup(Long orderId, Long userId);

    /**
     * 获取订单统计
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    Map<String, Object> getOrderStatistics(Long userId);

    /**
     * 支付订单
     */
    void payOrder(Long orderId, Integer paymentMethod);

    /**
     * 确认收货
     */
    void confirmReceipt(Long orderId);

    /**
     * 根据ID获取订单
     */
    Order getOrderById(Long id);

    /**
     * 根据订单号获取订?     */
    Order getOrderByOrderNo(String orderNo);

    /**
     * 获取订单明细列表
     */
    List<OrderItem> getOrderItems(Long orderId);

    /**
     * 回滚订单库存
     */
    void rollbackStock(Long orderId);

    /**
     * 订单支付成功
     */
    void paySuccess(Long orderId);

    // ==================== 商家订单管理接口 ====================

    /**
     * 商家接单
     *
     * @param orderId    订单ID
     * @param merchantId 商家ID
     */
    void acceptOrder(Long orderId, Long merchantId);

    /**
     * 商家拒绝接单
     *
     * @param orderId    订单ID
     * @param merchantId 商家ID
     * @param reason     拒绝原因
     */
    void refuseOrder(Long orderId, Long merchantId, String reason);

    /**
     * 商家拒绝订单
     *
     * @param orderId    订单ID
     * @param merchantId 商家ID
     * @param reason     拒绝原因
     */
    void rejectOrder(Long orderId, Long merchantId, String reason);

    /**
     * 商家开始制?     *
     * @param orderId    订单ID
     * @param merchantId 商家ID
     */
    void startMaking(Long orderId, Long merchantId);

    /**
     * 商家准备订单（开始制作）
     *
     * @param orderId    订单ID
     * @param merchantId 商家ID
     */
    void prepareOrder(Long orderId, Long merchantId);

    /**
     * 商家完成制作，待取餐
     *
     * @param orderId    订单ID
     * @param merchantId 商家ID
     */
    void finishMaking(Long orderId, Long merchantId);

    /**
     * 商家完成订单
     *
     * @param orderId    订单ID
     * @param merchantId 商家ID
     */
    void completeOrder(Long orderId, Long merchantId);

    /**
     * 获取商家的订单列?     *
     * @param request 查询条件
     * @return 订单列表
     */
    Map<String, Object> getMerchantOrderList(OrderListRequest request);

    /**
     * 获取商家的订单列?     *
     * @param merchantId 商家ID
     * @param status     订单状态（可选）
     * @param page       页码
     * @param size       每页大小
     * @return 订单列表
     */
    OrderListResponse getMerchantOrderList(Long merchantId, Integer status, int page, int size);
}
