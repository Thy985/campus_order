package com.xingchen.backend.config.payment;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信支付配置属性
 *
 * @author xingchen
 * @date 2026-02-13
 */
@Data
@Component
@ConfigurationProperties(prefix = "payment.wechat")
public class WeChatPayProperties {

    /**
     * 应用ID（公众号/小程序AppID）
     */
    private String appId;

    /**
     * 商户号
     */
    private String mchId;

    /**
     * API v3密钥（32位）
     */
    private String apiV3Key;

    /**
     * 商户API证书序列号
     */
    private String merchantSerialNumber;

    /**
     * 商户私钥文件路径
     */
    private String privateKeyPath;

    /**
     * 支付回调通知URL
     */
    private String notifyUrl;

    /**
     * 退款回调通知URL
     */
    private String refundNotifyUrl;

    /**
     * 是否启用沙箱环境
     */
    private Boolean sandbox = false;

    /**
     * 支付超时时间（分钟）
     */
    private Integer timeout = 15;
}
