package com.xingchen.backend.config.payment;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * 支付宝配置类
 *
 * @author xingchen
 * @date 2026-02-15
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AlipayConfig {

    private final AlipayProperties alipayProperties;

    @PostConstruct
    public void init() {
        log.info("支付宝配置初始化完成，沙箱环境: {}", alipayProperties.getSandbox());
        log.info("支付宝网关: {}", alipayProperties.getGatewayUrl());
        // 打印公钥前50个字符用于调试（不要打印完整密钥）
        String publicKey = alipayProperties.getPublicKey();
        if (publicKey != null && publicKey.length() > 50) {
            log.info("支付宝公钥前50字符: {}...", publicKey.substring(0, 50));
        }
    }

    /**
     * 创建支付宝客户端
     */
    @Bean
    public AlipayClient alipayClient() {
        // 直接使用配置中的公钥，不进行额外格式化
        // 支付宝SDK会自动处理公钥格式
        String publicKey = alipayProperties.getPublicKey();

        return new DefaultAlipayClient(
                alipayProperties.getGatewayUrl(),
                alipayProperties.getAppId(),
                alipayProperties.getPrivateKey(),
                "json",
                alipayProperties.getCharset(),
                publicKey,
                alipayProperties.getSignType()
        );
    }
}
