package com.xingchen.backend.listener;

import com.xingchen.backend.event.NewOrderEvent;
import com.xingchen.backend.event.OrderPaidEvent;
import com.xingchen.backend.event.OrderStatusChangeEvent;
import com.xingchen.backend.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 订单事件监听器
 * 在事务提交后触发 WebSocket 通知
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final WebSocketService webSocketService;

    /**
     * 监听订单状态变更事件
     * AFTER_COMMIT 确保事务提交后才发送通知
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderStatusChange(OrderStatusChangeEvent event) {
        log.info("事务提交后，发送订单状态变更通知: orderId={}, status={}", 
                event.getOrderId(), event.getNewStatus());
        
        try {
            // 通知用户
            if (event.getUserId() != null) {
                webSocketService.notifyUserOrderStatus(
                        event.getUserId(), 
                        event.getOrderId(), 
                        event.getNewStatus(), 
                        event.getMessage());
            }
            
            // 通知商家
            if (event.getMerchantId() != null) {
                webSocketService.notifyMerchantOrderStatus(
                        event.getMerchantId(), 
                        event.getOrderId(), 
                        event.getNewStatus(), 
                        event.getMessage());
            }
        } catch (Exception e) {
            log.error("发送订单状态变更通知失败: orderId={}", event.getOrderId(), e);
        }
    }

    /**
     * 监听新订单事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNewOrder(NewOrderEvent event) {
        log.info("事务提交后，发送新订单通知: orderId={}, merchantId={}", 
                event.getOrderId(), event.getMerchantId());
        
        try {
            if (event.getMerchantId() != null) {
                webSocketService.notifyMerchantNewOrder(
                        event.getMerchantId(), 
                        event.getOrderId(), 
                        event.getMessage());
            }
        } catch (Exception e) {
            log.error("发送新订单通知失败: orderId={}", event.getOrderId(), e);
        }
    }

    /**
     * 监听订单支付成功事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderPaid(OrderPaidEvent event) {
        log.info("事务提交后，发送订单支付成功通知: orderId={}", event.getOrderId());
        
        try {
            // 通知用户订单已支付
            if (event.getUserId() != null) {
                webSocketService.notifyUserOrderStatus(
                        event.getUserId(), 
                        event.getOrderId(), 
                        2, // 待接单状态
                        "订单支付成功，等待商家接单");
            }
            
            // 通知商家有新订单
            if (event.getMerchantId() != null) {
                webSocketService.notifyMerchantNewOrder(
                        event.getMerchantId(), 
                        event.getOrderId(), 
                        "收到新订单，金额：¥" + event.getAmount());
            }
        } catch (Exception e) {
            log.error("发送支付成功通知失败: orderId={}", event.getOrderId(), e);
        }
    }
}
