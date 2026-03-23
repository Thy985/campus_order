package com.xingchen.backend.service.payment;

import com.xingchen.backend.entity.Payment;
import com.xingchen.backend.dto.request.payment.WeChatPayCallbackRequest;
import com.xingchen.backend.dto.response.payment.WeChatPayResponse;

/**
 * 微信支付服务接口
 * 
 * @author 小跃
 * @date 2026-02-13
 */
public interface WeChatPayService {
    
    /**
     * 创建支付订单（JSAPI支付 - 小程?公众号）
     * 
     * @param payment 支付记录
     * @return 支付参数
     */
    WeChatPayResponse createJsApiPay(Payment payment);
    
    /**
     * 创建支付订单（APP支付?     * 
     * @param payment 支付记录
     * @return 支付参数
     */
    WeChatPayResponse createAppPay(Payment payment);
    
    /**
     * 创建支付订单（H5支付?     * 
     * @param payment 支付记录
     * @return 支付参数
     */
    WeChatPayResponse createH5Pay(Payment payment);
    
    /**
     * 创建支付订单（Native支付 - 扫码支付?     * 
     * @param payment 支付记录
     * @return 支付参数
     */
    WeChatPayResponse createNativePay(Payment payment);
    
    /**
     * 处理支付回调通知
     * 
     * @param requestBody 请求?     * @param signature 签名
     * @param serial 证书序列?     * @param timestamp 时间?     * @param nonce 随机字符?     * @return 是否处理成功
     */
    boolean handlePaymentCallback(String requestBody, String signature, String serial, String timestamp, String nonce);
    
    /**
     * 查询支付订单
     * 
     * @param transactionId 微信支付订单?     * @return 支付订单信息
     */
    String queryPayment(String transactionId);
    
    /**
     * 关闭支付订单
     * 
     * @param outTradeNo 商户订单?     * @return 是否关闭成功
     */
    boolean closePayment(String outTradeNo);
    
    /**
     * 申请退?     * 
     * @param outTradeNo 商户订单?     * @param outRefundNo 商户退款单?     * @param refundAmount 退款金额（分）
     * @param totalAmount 订单总金额（分）
     * @param reason 退款原?     * @return 退款结?     */
    String refund(String outTradeNo, String outRefundNo, Long refundAmount, Long totalAmount, String reason);
}
