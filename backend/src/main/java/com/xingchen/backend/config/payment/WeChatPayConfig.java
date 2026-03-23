package com.xingchen.backend.config.payment;

import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.cert.CertificatesManager;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WeChatPayConfig {

    private final WeChatPayProperties weChatPayProperties;

    @Bean
    public PrivateKey merchantPrivateKey() {
        try {
            String privateKeyPath = weChatPayProperties.getPrivateKeyPath();
            if (privateKeyPath == null || privateKeyPath.isEmpty()) {
                log.warn("WeChat Pay merchant private key path not configured");
                return null;
            }

            return PemUtil.loadPrivateKey(new FileInputStream(privateKeyPath));
        } catch (IOException e) {
            log.error("Failed to load WeChat Pay merchant private key", e);
            return null;
        }
    }

    @Bean
    public CertificatesManager certificatesManager() {
        PrivateKey merchantPrivateKey = merchantPrivateKey();
        if (merchantPrivateKey == null) {
            log.warn("Merchant private key not loaded, skipping certificates manager initialization");
            return null;
        }

        try {
            PrivateKeySigner privateKeySigner = new PrivateKeySigner(
                    weChatPayProperties.getMerchantSerialNumber(),
                    merchantPrivateKey
            );

            WechatPay2Credentials wechatPay2Credentials = new WechatPay2Credentials(
                    weChatPayProperties.getMchId(),
                    privateKeySigner
            );

            CertificatesManager certificatesManager = CertificatesManager.getInstance();
            certificatesManager.putMerchant(
                    weChatPayProperties.getMchId(),
                    wechatPay2Credentials,
                    weChatPayProperties.getApiV3Key().getBytes(StandardCharsets.UTF_8)
            );

            log.info("WeChat Pay certificates manager initialized successfully");
            return certificatesManager;
        } catch (Exception e) {
            log.error("Failed to initialize WeChat Pay certificates manager", e);
            return null;
        }
    }

    @Bean
    public CloseableHttpClient httpClient() {
        PrivateKey merchantPrivateKey = merchantPrivateKey();
        CertificatesManager certificatesManager = certificatesManager();

        if (merchantPrivateKey == null || certificatesManager == null) {
            log.warn("WeChat Pay configuration incomplete, skipping HttpClient initialization");
            return null;
        }

        try {
            Verifier verifier = certificatesManager.getVerifier(weChatPayProperties.getMchId());

            WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                    .withMerchant(
                            weChatPayProperties.getMchId(),
                            weChatPayProperties.getMerchantSerialNumber(),
                            merchantPrivateKey
                    )
                    .withValidator(new WechatPay2Validator(verifier));

            CloseableHttpClient httpClient = builder.build();
            log.info("WeChat Pay HttpClient initialized successfully");
            return httpClient;
        } catch (Exception e) {
            log.error("Failed to initialize WeChat Pay HttpClient", e);
            return null;
        }
    }
}
