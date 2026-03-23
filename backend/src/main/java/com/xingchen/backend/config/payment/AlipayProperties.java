package com.xingchen.backend.config.payment;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝配置属性
 *
 * @author xingchen
 * @date 2026-02-15
 */
@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayProperties {

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 商户私钥
     */
    private String privateKey;

    /**
     * 支付宝公钥
     */
    private String publicKey;

    /**
     * 服务器异步通知页面路径
     */
    private String notifyUrl;

    /**
     * 页面跳转同步通知页面路径
     */
    private String returnUrl;

    /**
     * 签名方式
     */
    private String signType = "RSA2";

    /**
     * 字符编码格式
     */
    private String charset = "UTF-8";

    /**
     * 支付宝网关
     */
    private String gatewayUrl;

    /**
     * 是否沙箱环境
     */
    private Boolean sandbox = true;

    /**
     * 沙箱环境网关
     */
    public static final String SANDBOX_GATEWAY = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    /**
     * 正式环境网关
     */
    public static final String PROD_GATEWAY = "https://openapi.alipay.com/gateway.do";

    /**
     * 获取网关地址
     */
    public String getGatewayUrl() {
        if (gatewayUrl != null && !gatewayUrl.isEmpty()) {
            return gatewayUrl;
        }
        return Boolean.TRUE.equals(sandbox) ? SANDBOX_GATEWAY : PROD_GATEWAY;
    }
}
