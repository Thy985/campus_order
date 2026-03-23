package com.xingchen.backend.util.payment;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;

/**
 * 支付验证工具类
 * 用于支付签名验证、防重放攻击等
 *
 * @author 小跃
 * @date 2026-02-14
 */
@Slf4j
public class PaymentValidator {

    /**
     * 验证微信支付回调签名
     *
     * @param timestamp 时间戳
     * @param nonce     随机数
     * @param body      请求体
     * @param signature 签名
     * @param apiV3Key  APIv3密钥
     * @return 是否验证通过
     */
    public static boolean verifyWeChatPaySignature(String timestamp, String nonce, String body, String signature, String apiV3Key) {
        try {
            // 构造验签串
            String signStr = timestamp + "\n" + nonce + "\n" + body + "\n";
            
            // 使用商户APIv3密钥进行SHA256-HMAC加密
            HMac hMac = new HMac(HmacAlgorithm.HmacSHA256, apiV3Key.getBytes(StandardCharsets.UTF_8));
            String calculatedSignature = hMac.digestHex(signStr);
            
            boolean result = calculatedSignature.equalsIgnoreCase(signature);
            
            if (!result) {
                log.warn("微信支付签名验证失败!");
                log.warn("期望签名: {}", calculatedSignature);
                log.warn("实际签名: {}", signature);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("验证微信支付签名异常", e);
            return false;
        }
    }

    /**
     * 验证时间戳是否在有效期内(防重放攻击)
     *
     * @param timestamp 时间戳（秒）
     * @param validSeconds 有效时间(秒)
     * @return 是否有效
     */
    public static boolean verifyTimestamp(String timestamp, long validSeconds) {
        try {
            long requestTime = Long.parseLong(timestamp);
            long currentTime = System.currentTimeMillis() / 1000;
            long diff = Math.abs(currentTime - requestTime);
            
            boolean valid = diff <= validSeconds;
            
            if (!valid) {
                log.warn("时间戳验证失败! 请求时间: {}, 当前时间: {}, 差值: {}秒", 
                    requestTime, currentTime, diff);
            }
            
            return valid;
            
        } catch (NumberFormatException e) {
            log.error("时间戳格式错误: {}", timestamp);
            return false;
        }
    }

    /**
     * 生成支付签名(通用方法)
     *
     * @param params 参数Map
     * @param key    密钥
     * @return 签名字符串
     */
    public static String generateSign(Map<String, String> params, String key) {
        // 按参数名ASCII码从小到大排序
        TreeMap<String, String> sortedParams = new TreeMap<>(params);
        
        // 拼接参数
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            String value = entry.getValue();
            if (value != null && !value.isEmpty()) {
                sb.append(entry.getKey()).append("=").append(value).append("&");
            }
        }
        
        // 添加key
        sb.append("key=").append(key);
        
        // MD5签名
        String sign = DigestUtil.md5Hex(sb.toString()).toUpperCase();
        
        log.debug("签名原串: {}", sb.toString());
        log.debug("生成签名: {}", sign);
        
        return sign;
    }

    /**
     * 验证支付签名
     *
     * @param params 参数Map(包含sign字段)
     * @param key    密钥
     * @return 是否验证通过
     */
    public static boolean verifySign(Map<String, String> params, String key) {
        String sign = params.get("sign");
        if (sign == null || sign.isEmpty()) {
            log.warn("签名字段为空");
            return false;
        }
        
        // 移除sign字段后重新生成签名
        Map<String, String> paramsWithoutSign = new TreeMap<>(params);
        paramsWithoutSign.remove("sign");
        
        String calculatedSign = generateSign(paramsWithoutSign, key);
        boolean result = calculatedSign.equals(sign);
        
        if (!result) {
            log.warn("签名验证失败! 期望: {}, 实际: {}", calculatedSign, sign);
        }
        
        return result;
    }

    /**
     * 验证支付金额是否一致
     *
     * @param expectedAmount 期望金额(分)
     * @param actualAmount   实际金额(分)
     * @return 是否一致
     */
    public static boolean verifyAmount(Long expectedAmount, Long actualAmount) {
        boolean match = expectedAmount.equals(actualAmount);
        
        if (!match) {
            log.error("金额验证失败! 期望: {} 分, 实际: {} 分", expectedAmount, actualAmount);
        }
        
        return match;
    }

    /**
     * 验证订单号是否合法
     *
     * @param orderNo 订单号
     * @return 是否合法
     */
    public static boolean verifyOrderNo(String orderNo) {
        if (orderNo == null || orderNo.isEmpty()) {
            log.warn("订单号为空");
            return false;
        }
        
        // 订单号格式验证(根据实际业务调整)
        if (!orderNo.matches("^[A-Z0-9]{20,32}$")) {
            log.warn("订单号格式不合法: {}", orderNo);
            return false;
        }
        
        return true;
    }

    /**
     * 验证支付状态是否合法
     *
     * @param status 支付状态
     * @return 是否合法
     */
    public static boolean verifyPaymentStatus(String status) {
        // 微信支付状态: SUCCESS-支付成功, REFUND-转入退款, NOTPAY-未支付, CLOSED-已关闭, 
        // REVOKED-已撤销(刷卡支付), USERPAYING-用户支付中, PAYERROR-支付失败
        String[] validStatuses = {"SUCCESS", "REFUND", "NOTPAY", "CLOSED", "REVOKED", "USERPAYING", "PAYERROR"};
        
        for (String validStatus : validStatuses) {
            if (validStatus.equals(status)) {
                return true;
            }
        }
        
        log.warn("不合法的支付状态: {}", status);
        return false;
    }

    /**
     * SHA256加密
     *
     * @param data 原始数据
     * @return 加密后的十六进制字符串
     */
    public static String sha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            
            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (Exception e) {
            log.error("SHA256加密失败", e);
            return null;
        }
    }

    /**
     * 生成随机字符串
     *
     * @param length 长度
     * @return 随机字符串
     */
    public static String generateNonceStr(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        
        return sb.toString();
    }

    /**
     * 验证回调是否重复处理(需配合缓存使用)
     *
     * @param cacheKey 缓存key(通常是支付单号或微信支付单号)
     * @param cacheValue 缓存值(可以是时间戳)
     * @return true-首次处理, false-重复处理
     */
    public static boolean isFirstTimeCallback(String cacheKey, String cacheValue) {
        // 这里需要配合Redis等缓存实现
        // 伪代码示例:
        // if (redisTemplate.hasKey(cacheKey)) {
        //     log.warn("重复的支付回调: {}", cacheKey);
        //     return false;
        // }
        // redisTemplate.opsForValue().set(cacheKey, cacheValue, 1, TimeUnit.HOURS);
        // return true;
        
        log.debug("验证回调是否重复: cacheKey={}, cacheValue={}", cacheKey, cacheValue);
        return true; // 默认返回true,实际使用时需要集成缓存
    }
}
