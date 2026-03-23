package com.xingchen.backend.dto.request.payment;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    
    /**
     * 订单?     */
    private String orderNo;
    
    /**
     * 支付金额
     */
    private BigDecimal amount;
    
    /**
     * 支付方式
     */
    private Integer paymentMethod;
    
    /**
     * 商品描述
     */
    private String description;
    
    /**
     * 回调地址
     */
    private String callbackUrl;
}
