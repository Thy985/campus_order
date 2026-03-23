package com.xingchen.backend.dto.request.payment;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentCallbackRequest {
    
    /**
     * 订单?     */
    private String orderNo;
    
    /**
     * 支付单号
     */
    private String paymentNo;
    
    /**
     * 交易流水?     */
    private String tradeNo;
    
    /**
     * 支付金额
     */
    private BigDecimal amount;
    
    /**
     * 支付状?     */
    private Integer status;
    
    /**
     * 支付方式
     */
    private Integer paymentMethod;
    
    /**
     * 支付时间
     */
    private String paymentTime;
    
    /**
     * 交易流水?     */
    private String transactionId;
    
    /**
     * 签名
     */
    private String sign;
}
