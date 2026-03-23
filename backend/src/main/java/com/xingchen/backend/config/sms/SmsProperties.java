package com.xingchen.backend.config.sms;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 短信服务配置
 * 支持阿里云短信、腾讯云短信
 *
 * @author 小跃
 * @date 2026-02-14
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "sms")
public class SmsProperties {

    /**
     * 短信服务提供商
     * aliyun - 阿里云短信
     * tencent - 腾讯云短信
     * mock - 模拟短信服务(开发环境使用)
     */
    private String provider = "mock";

    /**
     * 是否启用短信服务
     */
    private Boolean enabled = false;

    /**
     * AccessKey ID
     */
    private String accessKeyId;

    /**
     * AccessKey Secret
     */
    private String accessKeySecret;

    /**
     * 短信签名
     */
    private String signName;

    /**
     * 阿里云短信配置
     */
    private AliyunSmsConfig aliyun = new AliyunSmsConfig();

    /**
     * 腾讯云短信配置
     */
    private TencentSmsConfig tencent = new TencentSmsConfig();

    /**
     * 短信模板配置
     */
    private SmsTemplate template = new SmsTemplate();

    @Data
    public static class AliyunSmsConfig {
        /**
         * 区域ID
         */
        private String regionId = "cn-hangzhou";

        /**
         * 端点
         */
        private String endpoint = "dysmsapi.aliyuncs.com";
    }

    @Data
    public static class TencentSmsConfig {
        /**
         * SDK AppId
         */
        private String sdkAppId;

        /**
         * 区域
         */
        private String region = "ap-guangzhou";
    }

    @Data
    public static class SmsTemplate {
        /**
         * 验证码模板ID
         */
        private String verifyCode;

        /**
         * 订单超时取消模板ID
         */
        private String orderTimeout;

        /**
         * 支付成功模板ID
         */
        private String paymentSuccess;

        /**
         * 订单状态变更模板ID
         */
        private String orderStatusChange;

        /**
         * 新订单通知模板ID(商家)
         */
        private String newOrderNotify;

        /**
         * 订单完成通知模板ID
         */
        private String orderComplete;
    }
}
