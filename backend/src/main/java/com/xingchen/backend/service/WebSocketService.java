package com.xingchen.backend.service;

/**
 * WebSocket推送服务接口
 */
public interface WebSocketService {

    /**
     * 通知用户订单状态变更
     */
    void notifyUserOrderStatus(Long userId, Long orderId, Integer status, String message);

    /**
     * 通知商家有新订单
     */
    void notifyMerchantNewOrder(Long merchantId, Long orderId, String message);

    /**
     * 通知商家订单状态变更
     */
    void notifyMerchantOrderStatus(Long merchantId, Long orderId, Integer status, String message);
}
