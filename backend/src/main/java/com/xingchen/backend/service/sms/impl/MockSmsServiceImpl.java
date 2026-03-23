package com.xingchen.backend.service.sms.impl;

import com.xingchen.backend.config.sms.SmsProperties;
import com.xingchen.backend.service.sms.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 模拟短信服务实现
 * 用于开发环境，不实际发送短信，只记录日志
 *
 * @author 小跃
 * @date 2026-02-14
 */
@Slf4j
@Service("mockSmsService")
@ConditionalOnProperty(prefix = "sms", name = "provider", havingValue = "mock", matchIfMissing = true)
@RequiredArgsConstructor
public class MockSmsServiceImpl implements SmsService {

    private final SmsProperties smsProperties;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public boolean sendSms(String phone, String templateCode, Map<String, String> params) {
        log.info("========== 模拟发送短信 ==========");
        log.info("发送时间: {}", LocalDateTime.now().format(FORMATTER));
        log.info("手机号: {}", maskPhone(phone));
        log.info("短信签名: {}", smsProperties.getSignName());
        log.info("模板代码: {}", templateCode);
        log.info("模板参数: {}", params);
        log.info("短信内容: {}", buildSmsContent(templateCode, params));
        log.info("发送状态: 成功(模拟)");
        log.info("===================================");
        return true;
    }

    @Override
    public boolean sendVerifyCode(String phone, String code) {
        log.info("========== 发送验证码短信 ==========");
        log.info("手机号: {}", maskPhone(phone));
        log.info("验证码: {}", code);
        log.info("短信内容: 【{}】您的验证码是: {}，5分钟内有效，请勿泄露给他人",
            smsProperties.getSignName(), code);
        log.info("====================================");
        return sendSms(phone, smsProperties.getTemplate().getVerifyCode(), Map.of("code", code));
    }

    @Override
    public boolean sendOrderNotification(String phone, String orderNo, String amount) {
        log.info("========== 发送订单通知 ==========");
        log.info("手机号: {}", maskPhone(phone));
        log.info("订单号: {}", orderNo);
        log.info("订单金额: {} 元", amount);
        log.info("短信内容: 【{}】您有新订单{}，金额{}元，请及时查看",
            smsProperties.getSignName(), orderNo, amount);
        log.info("=================================");
        return sendSms(phone, smsProperties.getTemplate().getNewOrderNotify(), 
            Map.of("orderNo", orderNo, "amount", amount));
    }

    @Override
    public boolean sendOrderTimeoutNotification(String phone, String orderNo) {
        log.info("========== 发送订单超时通知 ==========");
        log.info("手机? {}", maskPhone(phone));
        log.info("订单? {}", orderNo);
        log.info("短信内容: 【{}】您的订单{}因超时未支付已自动取?如有疑问请联系客服?", 
            smsProperties.getSignName(), orderNo);
        log.info("======================================");
        return sendSms(phone, smsProperties.getTemplate().getOrderTimeout(), 
            Map.of("orderNo", orderNo));
    }

    @Override
    public boolean sendPaymentSuccessNotification(String phone, String orderNo, String amount) {
        log.info("========== 发送支付成功通知 ==========");
        log.info("手机号: {}", maskPhone(phone));
        log.info("订单号: {}", orderNo);
        log.info("支付金额: {} 元", amount);
        log.info("短信内容: 【{}】您的订单{}支付成功，金额{}元，商家正在准备中",
            smsProperties.getSignName(), orderNo, amount);
        log.info("======================================");
        return sendSms(phone, smsProperties.getTemplate().getPaymentSuccess(), 
            Map.of("orderNo", orderNo, "amount", amount));
    }

    @Override
    public boolean sendOrderStatusChangeNotification(String phone, String orderNo, String statusText) {
        log.info("========== 发送订单状态变更通知 ==========");
        log.info("手机号: {}", maskPhone(phone));
        log.info("订单号: {}", orderNo);
        log.info("订单状态: {}", statusText);
        log.info("短信内容: 【{}】您的订单{}状态已更新为: {}",
            smsProperties.getSignName(), orderNo, statusText);
        log.info("==========================================");
        return sendSms(phone, smsProperties.getTemplate().getOrderStatusChange(), 
            Map.of("orderNo", orderNo, "status", statusText));
    }

    @Override
    public boolean sendNewOrderNotification(String phone, String orderNo, String amount) {
        log.info("========== 发送新订单通知(商家) ==========");
        log.info("手机号: {}", maskPhone(phone));
        log.info("订单号: {}", orderNo);
        log.info("订单金额: {} 元", amount);
        log.info("短信内容: 【{}】您收到新订单{}，金额{}元，请及时处理",
            smsProperties.getSignName(), orderNo, amount);
        log.info("=========================================");
        return sendSms(phone, smsProperties.getTemplate().getNewOrderNotify(), 
            Map.of("orderNo", orderNo, "amount", amount));
    }

    @Override
    public int sendBatchSms(String[] phones, String templateCode, Map<String, String> params) {
        log.info("========== 批量发送短信 ==========");
        log.info("接收人数: {}", phones.length);
        log.info("模板代码: {}", templateCode);
        log.info("模板参数: {}", params);
        
        int successCount = 0;
        for (String phone : phones) {
            if (sendSms(phone, templateCode, params)) {
                successCount++;
            }
        }
        
        log.info("发送完成: 成功={}, 失败={}", successCount, phones.length - successCount);
        log.info("==================================");
        return successCount;
    }

    @Override
    public boolean batchSendSms(List<String> phones, String templateCode) {
        log.info("========== 批量发送短信(List) ==========");
        log.info("接收人数: {}", phones.size());
        log.info("模板代码: {}", templateCode);
        
        String[] phoneArray = phones.toArray(new String[0]);
        int successCount = sendBatchSms(phoneArray, templateCode, null);
        
        return successCount == phones.size();
    }

    /**
     * 手机号脱敏处理
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 构建短信内容(用于日志展示)
     */
    private String buildSmsContent(String templateCode, Map<String, String> params) {
        // 根据模板代码返回不同的短信内容
        if (templateCode == null) {
            return "未知模板";
        }

        if (templateCode.equals(smsProperties.getTemplate().getVerifyCode())) {
            return String.format("【%s】您的验证码是: %s，5分钟内有效，请勿泄露给他人",
                smsProperties.getSignName(), params.get("code"));
        } else if (templateCode.equals(smsProperties.getTemplate().getOrderTimeout())) {
            return String.format("【%s】您的订单%s因超时未支付已自动取消，如有疑问请联系客服",
                smsProperties.getSignName(), params.get("orderNo"));
        } else if (templateCode.equals(smsProperties.getTemplate().getPaymentSuccess())) {
            return String.format("【%s】您的订单%s支付成功，金额%s元，商家正在准备中",
                smsProperties.getSignName(), params.get("orderNo"), params.get("amount"));
        } else if (templateCode.equals(smsProperties.getTemplate().getOrderStatusChange())) {
            return String.format("【%s】您的订单%s状态已更新为: %s",
                smsProperties.getSignName(), params.get("orderNo"), params.get("status"));
        } else if (templateCode.equals(smsProperties.getTemplate().getNewOrderNotify())) {
            return String.format("【%s】您收到新订单%s，金额%s元，请及时处理",
                smsProperties.getSignName(), params.get("orderNo"), params.get("amount"));
        } else {
            return String.format("【%s】通用短信模板，参数: %s",
                smsProperties.getSignName(), params);
        }
    }
}
