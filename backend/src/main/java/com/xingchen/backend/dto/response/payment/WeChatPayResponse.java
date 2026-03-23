package com.xingchen.backend.dto.response.payment;

import lombok.Data;

import java.util.Map;

/**
 * 微信支付响应
 * 
 * @author 小跃
 * @date 2026-02-13
 */
@Data
public class WeChatPayResponse {
    
    /**
     * 预支付交易会话标识（JSAPI）
     */
    private String prepayId;
    
    /**
     * 二维码链接（Native）
     */
    private String codeUrl;
    
    /**
     * 支付跳转链接（H5）
     */
    private String h5Url;
    
    /**
     * 支付参数（用于前端调起支付）
     */
    private Map<String, String> paymentParams;
    
    /**
     * 商户订单号
     */
    private String outTradeNo;
    
    /**
     * 支付单号
     */
    private String paymentNo;
}
