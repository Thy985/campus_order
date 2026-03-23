package com.xingchen.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付系统配置类
 *
 * <p>从application.yaml读取支付相关配置</p>
 *
 * <p>配置项前缀：payment</p>
 * <pre>
 * payment:
 *   secret-key: ${PAYMENT_SECRET_KEY}
 *   notify-url: ${PAYMENT_NOTIFY_URL}
 *   timeout: 30
 * </pre>
 *
 * @author xingchen
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "payment")
public class PaymentConfig {

    /** 支付密钥，用于签名验证 */
    private String secretKey;

    /** 支付回调通知URL */
    private String notifyUrl;

    /** 支付超时时间（分钟） */
    private int timeout;

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
