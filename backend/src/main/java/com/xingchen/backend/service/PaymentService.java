package com.xingchen.backend.service;

import com.xingchen.backend.dto.request.payment.PaymentCallbackRequest;
import com.xingchen.backend.dto.request.payment.PaymentRequest;
import com.xingchen.backend.dto.response.payment.PaymentResponse;
import com.xingchen.backend.entity.Payment;

public interface PaymentService {
    
    /**
     * 生成支付链接
     */
    PaymentResponse generatePaymentUrl(PaymentRequest request);
    
    /**
     * 处理支付回调
     */
    void handlePaymentCallback(PaymentCallbackRequest request);
    
    /**
     * 查询支付状态
     */
    Integer queryPaymentStatus(String orderNo);
    
    /**
     * 关闭支付
     */
    void closePayment(String orderNo);
    
    /**
     * 根据订单号查询支付记录
     * 
     * @param orderNo 订单号
     * @return 支付记录
     */
    Payment getPaymentByOrderNo(String orderNo);
}
