package com.xingchen.backend.service.notification;

import com.xingchen.backend.entity.Order;

/**
 * 通知服务接口
 * 
 * @author 小跃
 * @date 2026-02-13
 */
public interface NotificationService {
    
    /**
     * 发送订单超时取消通知（给用户?     * 
     * @param order 订单
     */
    void sendOrderTimeoutNotification(Order order);
    
    /**
     * 发送订单支付成功通知（给用户和商家）
     * 
     * @param order 订单
     */
    void sendPaymentSuccessNotification(Order order);
    
    /**
     * 发送订单状态变更通知
     * 
     * @param order 订单
     * @param oldStatus 旧状?     * @param newStatus 新状?     */
    void sendOrderStatusChangeNotification(Order order, Integer oldStatus, Integer newStatus);
}
