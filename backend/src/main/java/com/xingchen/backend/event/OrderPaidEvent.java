package com.xingchen.backend.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

/**
 * 订单支付成功事件
 * 用于在事务提交后通知用户和商家
 */
@Getter
public class OrderPaidEvent extends ApplicationEvent {
    
    private final Long orderId;
    private final Long userId;
    private final Long merchantId;
    private final BigDecimal amount;
    private final String message;
    
    public OrderPaidEvent(Object source, Long orderId, Long userId, Long merchantId, 
                          BigDecimal amount, String message) {
        super(source);
        this.orderId = orderId;
        this.userId = userId;
        this.merchantId = merchantId;
        this.amount = amount;
        this.message = message;
    }
}
