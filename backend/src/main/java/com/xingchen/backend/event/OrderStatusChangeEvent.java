package com.xingchen.backend.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 订单状态变更事件
 * 用于在事务提交后触发 WebSocket 通知
 */
@Getter
public class OrderStatusChangeEvent extends ApplicationEvent {
    
    private final Long orderId;
    private final Long userId;
    private final Long merchantId;
    private final Integer oldStatus;
    private final Integer newStatus;
    private final String message;
    
    public OrderStatusChangeEvent(Object source, Long orderId, Long userId, Long merchantId, 
                                   Integer oldStatus, Integer newStatus, String message) {
        super(source);
        this.orderId = orderId;
        this.userId = userId;
        this.merchantId = merchantId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.message = message;
    }
}
