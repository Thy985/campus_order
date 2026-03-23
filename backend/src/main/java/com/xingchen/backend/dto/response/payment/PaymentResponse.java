package com.xingchen.backend.dto.response.payment;

import lombok.Data;

import java.util.Map;

@Data
public class PaymentResponse {
    
    /**
     * 支付链接
     */
    private String paymentUrl;
    
    /**
     * 订单?     */
    private String orderNo;
    
    /**
     * 支付状?     */
    private Integer status;
    
    /**
     * 支付方式
     */
    private Integer paymentMethod;
    
    /**
     * 支付参数
     */
    private Map<String, String> paymentParams;
}
