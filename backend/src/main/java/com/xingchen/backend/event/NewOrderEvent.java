package com.xingchen.backend.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 新订单事件
 * 用于在事务提交后通知商家
 */
@Getter
public class NewOrderEvent extends ApplicationEvent {
    
    private final Long orderId;
    private final Long merchantId;
    private final String message;
    
    public NewOrderEvent(Object source, Long orderId, Long merchantId, String message) {
        super(source);
        this.orderId = orderId;
        this.merchantId = merchantId;
        this.message = message;
    }
}
