package com.xingchen.backend.controller.payment;

import com.xingchen.backend.dto.request.payment.PaymentCallbackRequest;
import com.xingchen.backend.service.PaymentService;
import com.xingchen.backend.util.Result;
import com.xingchen.backend.util.constant.Constants;
import com.xingchen.backend.util.idempotent.IdempotentUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * 通用支付回调控制器
 *
 * 处理各种支付渠道的回调通知
 * 
 * 安全特性：
 * 1. 签名验证防止伪造回调
 * 2. 幂等性保护防止重复处理
 * 3. 参数完整性校验
 *
 * @author xingchen
 * @date 2026-03-14
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentCallbackController {

    private final PaymentService paymentService;
    private final IdempotentUtil idempotentUtil;

    @Value("${payment.callback.secret:}")
    private String callbackSecret;

    /**
     * 通用支付回调接口
     *
     * 用于测试和兼容各种支付渠道的回调
     * 
     * 请求示例：
     * {
     *   "orderNo": "ORDER123456",
     *   "transactionId": "TRANS789012",
     *   "status": "SUCCESS",
     *   "amount": "99.99",
     *   "timestamp": "1711094400000",
     *   "sign": "xxx"
     * }
     *
     * @param params 回调参数
     * @return 处理结果
     */
    @PostMapping("/callback")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> paymentCallback(@RequestBody Map<String, Object> params) {
        log.info("收到支付回调: {}", params);

        // 参数校验
        if (params == null || params.isEmpty()) {
            log.warn("支付回调参数为空");
            return Result.badRequest("回调参数不能为空");
        }

        // 获取关键参数
        String orderNo = (String) params.get("orderNo");
        String transactionId = (String) params.get("transactionId");
        String status = (String) params.get("status");
        String sign = (String) params.get("sign");

        if (orderNo == null || orderNo.trim().isEmpty()) {
            log.warn("支付回调缺少订单号");
            return Result.badRequest("订单号不能为空");
        }

        if (status == null || status.trim().isEmpty()) {
            log.warn("支付回调缺少状态");
            return Result.badRequest("支付状态不能为空");
        }

        // ========== 签名验证 ==========
        if (!verifySign(params)) {
            log.error("支付回调签名验证失败, orderNo: {}", orderNo);
            return Result.badRequest("签名验证失败");
        }

        log.info("处理支付回调 - 订单号: {}, 交易号: {}, 状态: {}", orderNo, transactionId, status);

        // ========== 幂等性保护 ==========
        String idempotentKey = "payment:callback:" + orderNo + ":" + transactionId;
        if (!idempotentUtil.checkAndMark(idempotentKey, 86400)) { // 24小时过期
            log.warn("支付回调已处理，跳过重复请求: {}", orderNo);
            return Result.success("回调已处理", null);
        }

        try {
            // 构建回调请求
            PaymentCallbackRequest callbackRequest = new PaymentCallbackRequest();
            callbackRequest.setOrderNo(orderNo);
            callbackRequest.setTransactionId(transactionId);

            // 根据状态处理
            if ("SUCCESS".equalsIgnoreCase(status)) {
                // 支付成功处理
                log.info("支付成功 - 订单号: {}", orderNo);
                callbackRequest.setStatus(Constants.PaymentStatus.PAID);
                paymentService.handlePaymentCallback(callbackRequest);
                log.info("支付成功处理完成 - 订单号: {}", orderNo);
            } else if ("FAIL".equalsIgnoreCase(status) || "FAILED".equalsIgnoreCase(status)) {
                // 支付失败处理
                log.warn("支付失败 - 订单号: {}", orderNo);
                callbackRequest.setStatus(Constants.PaymentStatus.FAILED);
                paymentService.handlePaymentCallback(callbackRequest);
                log.info("支付失败处理完成 - 订单号: {}", orderNo);
            } else {
                log.warn("未知的支付状态 - 订单号: {}, 状态: {}", orderNo, status);
                // 清除标记，允许后续正确处理
                idempotentUtil.clearMark(idempotentKey);
                return Result.badRequest("未知的支付状态: " + status);
            }

            return Result.success("支付回调处理成功", null);
        } catch (Exception e) {
            // 发生异常，清除标记，允许重试
            idempotentUtil.clearMark(idempotentKey);
            log.error("支付回调处理异常 - 订单号: {}, 错误: {}", orderNo, e.getMessage(), e);
            return Result.error("支付回调处理失败: " + e.getMessage());
        }
    }

    /**
     * 微信支付回调（兼容版本）
     */
    @PostMapping("/callback/wechat")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> wechatCallback(@RequestBody Map<String, Object> params) {
        log.info("收到微信支付回调(兼容接口): {}", params);
        return paymentCallback(params);
    }

    /**
     * 支付宝回调（兼容旧版本）
     */
    @PostMapping("/callback/alipay")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> alipayCallback(@RequestBody Map<String, Object> params) {
        log.info("收到支付宝回调(兼容接口): {}", params);
        return paymentCallback(params);
    }

    /**
     * 验证签名
     * 
     * 签名算法：
     * 1. 移除 sign 参数
     * 2. 按参数名升序排序
     * 3. 拼接成 key1=value1&key2=value2 格式
     * 4. 拼接密钥：string + "&key=" + secret
     * 5. MD5 加密（大写）
     *
     * @param params 回调参数
     * @return 签名是否有效
     */
    private boolean verifySign(Map<String, Object> params) {
        // 如果没有配置密钥，跳过验证（仅用于开发环境）
        if (callbackSecret == null || callbackSecret.trim().isEmpty()) {
            log.warn("未配置回调密钥，跳过签名验证");
            return true;
        }

        String sign = (String) params.get("sign");
        if (sign == null || sign.trim().isEmpty()) {
            log.error("签名参数为空");
            return false;
        }

        try {
            // 构建待签名字符串
            String signStr = buildSignString(params);
            
            // 计算签名
            String calculatedSign = md5(signStr + "&key=" + callbackSecret).toUpperCase();
            
            // 比较签名
            boolean verified = calculatedSign.equalsIgnoreCase(sign);
            if (!verified) {
                log.error("签名不匹配, 收到: {}, 计算: {}", sign, calculatedSign);
            }
            
            return verified;
        } catch (Exception e) {
            log.error("签名验证异常", e);
            return false;
        }
    }

    /**
     * 构建签名字符串
     */
    private String buildSignString(Map<String, Object> params) {
        // 移除 sign 参数
        Map<String, Object> signParams = new HashMap<>(params);
        signParams.remove("sign");

        // 按参数名升序排序
        List<String> keys = new ArrayList<>(signParams.keySet());
        Collections.sort(keys);

        // 拼接字符串
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            Object value = signParams.get(key);
            if (value != null && !value.toString().trim().isEmpty()) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(key).append("=").append(value);
            }
        }

        return sb.toString();
    }

    /**
     * MD5 加密
     */
    private String md5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }

    /**
     * HMAC-SHA256 签名（备用）
     */
    private String hmacSha256(String data, String key) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] bytes = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256签名失败", e);
        }
    }
}
