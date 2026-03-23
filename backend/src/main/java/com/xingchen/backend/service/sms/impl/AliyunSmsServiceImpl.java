package com.xingchen.backend.service.sms.impl;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.xingchen.backend.config.sms.SmsProperties;
import com.xingchen.backend.service.sms.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 阿里云短信服务实现
 * 
 * 使用说明:
 * 1. 在阿里云控制台开通短信服务
 * 2. 创建短信签名和短信模板
 * 3. 配置application.yaml中的短信参数
 * 4. 已添加Maven依赖: dysmsapi20170525
 *
 * @author 小跃
 * @date 2026-02-14
 */
@Slf4j
@Service("aliyunSmsService")
@ConditionalOnProperty(prefix = "sms", name = "provider", havingValue = "aliyun")
@RequiredArgsConstructor
public class AliyunSmsServiceImpl implements SmsService {

    private final SmsProperties smsProperties;

    /**
     * 阿里云短信客户端
     */
    private Client client;

    @PostConstruct
    public void init() throws Exception {
        Config config = new Config()
            .setAccessKeyId(smsProperties.getAccessKeyId())
            .setAccessKeySecret(smsProperties.getAccessKeySecret());
        config.endpoint = smsProperties.getAliyun().getEndpoint();
        this.client = new Client(config);
        log.info("阿里云短信客户端初始化成功");
    }

    @Override
    public boolean sendSms(String phone, String templateCode, Map<String, String> params) {
        if (!smsProperties.getEnabled()) {
            log.warn("短信服务未启用，跳过发送，phone={}, template={}", phone, templateCode);
            return false;
        }

        try {
            log.info("========== 发送阿里云短信 ==========");
            log.info("手机号: {}", phone);
            log.info("模板代码: {}", templateCode);
            log.info("模板参数: {}", params);

            // 手动构建JSON字符串
            StringBuilder jsonBuilder = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (!first) {
                    jsonBuilder.append(",");
                }
                jsonBuilder.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
                first = false;
            }
            jsonBuilder.append("}");
            
            SendSmsRequest request = new SendSmsRequest()
                .setPhoneNumbers(phone)
                .setSignName(smsProperties.getSignName())
                .setTemplateCode(templateCode)
                .setTemplateParam(jsonBuilder.toString());

            SendSmsResponse response = client.sendSms(request);
            
            if ("OK".equals(response.getBody().getCode())) {
                log.info("短信发送成功，BizId={}", response.getBody().getBizId());
                log.info("====================================");
                return true;
            } else {
                log.error("短信发送失败，Code={}, Message={}", 
                    response.getBody().getCode(), response.getBody().getMessage());
                log.info("====================================");
                return false;
            }

        } catch (Exception e) {
            log.error("发送阿里云短信异常: phone={}, template={}", phone, templateCode, e);
            return false;
        }
    }

    @Override
    public boolean sendVerifyCode(String phone, String code) {
        Map<String, String> params = new HashMap<>();
        params.put("code", code);
        return sendSms(phone, smsProperties.getTemplate().getVerifyCode(), params);
    }

    @Override
    public boolean sendOrderNotification(String phone, String orderNo, String amount) {
        Map<String, String> params = new HashMap<>();
        params.put("orderNo", orderNo);
        params.put("amount", amount);
        return sendSms(phone, smsProperties.getTemplate().getNewOrderNotify(), params);
    }

    @Override
    public boolean sendOrderTimeoutNotification(String phone, String orderNo) {
        Map<String, String> params = new HashMap<>();
        params.put("orderNo", orderNo);
        return sendSms(phone, smsProperties.getTemplate().getOrderTimeout(), params);
    }

    @Override
    public boolean sendPaymentSuccessNotification(String phone, String orderNo, String amount) {
        Map<String, String> params = new HashMap<>();
        params.put("orderNo", orderNo);
        params.put("amount", amount);
        return sendSms(phone, smsProperties.getTemplate().getPaymentSuccess(), params);
    }

    @Override
    public boolean sendOrderStatusChangeNotification(String phone, String orderNo, String statusText) {
        Map<String, String> params = new HashMap<>();
        params.put("orderNo", orderNo);
        params.put("status", statusText);
        return sendSms(phone, smsProperties.getTemplate().getOrderStatusChange(), params);
    }

    @Override
    public boolean sendNewOrderNotification(String phone, String orderNo, String amount) {
        Map<String, String> params = new HashMap<>();
        params.put("orderNo", orderNo);
        params.put("amount", amount);
        return sendSms(phone, smsProperties.getTemplate().getNewOrderNotify(), params);
    }

    @Override
    public int sendBatchSms(String[] phones, String templateCode, Map<String, String> params) {
        if (!smsProperties.getEnabled()) {
            log.warn("短信服务未启用，跳过批量发送");
            return 0;
        }

        int successCount = 0;
        for (String phone : phones) {
            if (sendSms(phone, templateCode, params)) {
                successCount++;
            }
        }

        log.info("批量发送短信完成，总数={}, 成功={}, 失败={}", 
            phones.length, successCount, phones.length - successCount);
        
        return successCount;
    }

    @Override
    public boolean batchSendSms(List<String> phones, String templateCode) {
        if (!smsProperties.getEnabled()) {
            log.warn("短信服务未启用，跳过批量发送");
            return false;
        }

        String[] phoneArray = phones.toArray(new String[0]);
        int successCount = sendBatchSms(phoneArray, templateCode, null);
        
        return successCount == phones.size();
    }
}
