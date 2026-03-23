package com.xingchen.backend.util.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 支付验证工具类测试
 *
 * @author 小跃
 * @date 2026-03-14
 */
class PaymentValidatorTest {

    private static final String API_KEY = "test_api_key_123456";

    // ==================== validateTimestamp 测试 ====================

    @Test
    @DisplayName("验证有效时间戳 - 当前时间")
    void testValidateTimestamp_Valid_CurrentTime() {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        boolean result = PaymentValidator.verifyTimestamp(timestamp, 300); // 5分钟有效期

        assertTrue(result);
    }

    @ParameterizedTest
    @CsvSource({
        "-10, 300",   // 10秒前
        "-60, 300",   // 1分钟前
        "-120, 300",  // 2分钟前
        "-10, 60",    // 10秒前，1分钟有效期
    })
    @DisplayName("验证有效时间戳 - 过去时间（在有效期内）")
    void testValidateTimestamp_Valid_PastTime(int offsetSeconds, long validSeconds) {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000 + offsetSeconds);
        boolean result = PaymentValidator.verifyTimestamp(timestamp, validSeconds);

        assertTrue(result);
    }

    @ParameterizedTest
    @CsvSource({
        "-301, 300",  // 5分钟1秒前，超过5分钟有效期
        "-600, 300",  // 10分钟前
        "-3600, 300", // 1小时前
    })
    @DisplayName("验证过期时间戳")
    void testValidateTimestamp_Expired(int offsetSeconds, long validSeconds) {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000 + offsetSeconds);
        boolean result = PaymentValidator.verifyTimestamp(timestamp, validSeconds);

        assertFalse(result);
    }

    @ParameterizedTest
    @CsvSource({
        "10, 300",    // 10秒后
        "60, 300",    // 1分钟后
        "120, 300",   // 2分钟后
    })
    @DisplayName("验证未来时间戳")
    void testValidateTimestamp_Future(int offsetSeconds, long validSeconds) {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000 + offsetSeconds);
        boolean result = PaymentValidator.verifyTimestamp(timestamp, validSeconds);

        // 未来时间戳在有效期内应该返回true（因为差值绝对值在范围内）
        assertTrue(result);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"abc", "not_a_number", "12.34"})
    @DisplayName("验证无效时间戳格式")
    void testValidateTimestamp_InvalidFormat(String timestamp) {
        boolean result = PaymentValidator.verifyTimestamp(timestamp, 300);

        assertFalse(result);
    }

    // ==================== validateSign 测试 ====================

    @Test
    @DisplayName("验证正确签名")
    void testValidateSign_Correct() {
        Map<String, String> params = new HashMap<>();
        params.put("appId", "test_app_id");
        params.put("orderNo", "ORDER123456");
        params.put("amount", "100");

        String sign = PaymentValidator.generateSign(params, API_KEY);
        params.put("sign", sign);

        boolean result = PaymentValidator.verifySign(params, API_KEY);

        assertTrue(result);
    }

    @Test
    @DisplayName("验证错误签名")
    void testValidateSign_Wrong() {
        Map<String, String> params = new HashMap<>();
        params.put("appId", "test_app_id");
        params.put("orderNo", "ORDER123456");
        params.put("amount", "100");

        // 使用错误的密钥生成签名
        String sign = PaymentValidator.generateSign(params, "wrong_key");
        params.put("sign", sign);

        boolean result = PaymentValidator.verifySign(params, API_KEY);

        assertFalse(result);
    }

    @Test
    @DisplayName("验证篡改参数后的签名")
    void testValidateSign_TamperedParams() {
        Map<String, String> params = new HashMap<>();
        params.put("appId", "test_app_id");
        params.put("orderNo", "ORDER123456");
        params.put("amount", "100");

        String sign = PaymentValidator.generateSign(params, API_KEY);
        params.put("sign", sign);

        // 篡改参数
        params.put("amount", "200");

        boolean result = PaymentValidator.verifySign(params, API_KEY);

        assertFalse(result);
    }

    @Test
    @DisplayName("验证空签名")
    void testValidateSign_EmptySign() {
        Map<String, String> params = new HashMap<>();
        params.put("appId", "test_app_id");
        params.put("orderNo", "ORDER123456");
        params.put("sign", "");

        boolean result = PaymentValidator.verifySign(params, API_KEY);

        assertFalse(result);
    }

    @Test
    @DisplayName("验证缺失签名字段")
    void testValidateSign_MissingSign() {
        Map<String, String> params = new HashMap<>();
        params.put("appId", "test_app_id");
        params.put("orderNo", "ORDER123456");
        // 没有sign字段

        boolean result = PaymentValidator.verifySign(params, API_KEY);

        assertFalse(result);
    }

    // ==================== validateAmount 测试 ====================

    @ParameterizedTest
    @CsvSource({
        "100, 100",
        "0, 0",
        "1, 1",
        "999999, 999999",
        "10000, 10000"
    })
    @DisplayName("验证正确金额")
    void testValidateAmount_Correct(Long expectedAmount, Long actualAmount) {
        boolean result = PaymentValidator.verifyAmount(expectedAmount, actualAmount);

        assertTrue(result);
    }

    @ParameterizedTest
    @CsvSource({
        "100, 200",
        "100, 99",
        "0, 1",
        "1, 0",
        "10000, 9999"
    })
    @DisplayName("验证错误金额")
    void testValidateAmount_Wrong(Long expectedAmount, Long actualAmount) {
        boolean result = PaymentValidator.verifyAmount(expectedAmount, actualAmount);

        assertFalse(result);
    }

    @Test
    @DisplayName("验证空金额")
    void testValidateAmount_Null() {
        // 验证 null 值 - 由于代码中使用 equals，null 会抛出 NullPointerException
        // 但 verifyAmount(100L, null) 不会抛出异常，因为 100L.equals(null) 返回 false
        assertThrows(NullPointerException.class, () -> PaymentValidator.verifyAmount(null, 100L));

        // 100L.equals(null) 返回 false，不会抛出异常
        boolean result = PaymentValidator.verifyAmount(100L, null);
        assertFalse(result);

        // null equals null 抛出 NullPointerException
        assertThrows(NullPointerException.class, () -> PaymentValidator.verifyAmount(null, null));
    }

    // ==================== 微信支付签名验证测试 ====================

    @Test
    @DisplayName("验证微信支付签名 - 正确")
    void testVerifyWeChatPaySignature_Correct() {
        String timestamp = "1704067200";
        String nonce = "test_nonce";
        String body = "{\"appid\":\"wx123\",\"mchid\":\"123456\"}";
        String apiV3Key = "test_api_v3_key_32bytes_long";

        // 先生成签名
        String signStr = timestamp + "\n" + nonce + "\n" + body + "\n";
        cn.hutool.crypto.digest.HMac hMac = new cn.hutool.crypto.digest.HMac(
                cn.hutool.crypto.digest.HmacAlgorithm.HmacSHA256,
                apiV3Key.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
        String signature = hMac.digestHex(signStr);

        boolean result = PaymentValidator.verifyWeChatPaySignature(
                timestamp, nonce, body, signature, apiV3Key
        );

        assertTrue(result);
    }

    @Test
    @DisplayName("验证微信支付签名 - 错误")
    void testVerifyWeChatPaySignature_Wrong() {
        String timestamp = "1704067200";
        String nonce = "test_nonce";
        String body = "{\"appid\":\"wx123\",\"mchid\":\"123456\"}";
        String apiV3Key = "test_api_v3_key_32bytes_long";
        String wrongSignature = "wrong_signature";

        boolean result = PaymentValidator.verifyWeChatPaySignature(
                timestamp, nonce, body, wrongSignature, apiV3Key
        );

        assertFalse(result);
    }

    // ==================== 订单号验证测试 ====================

    @ParameterizedTest
    @ValueSource(strings = {
        "ORDER123456789012345",
        "PAY20240314123456789",
        "ABC12345678901234567",
        "12345678901234567890"
    })
    @DisplayName("验证合法订单号")
    void testVerifyOrderNo_Valid(String orderNo) {
        boolean result = PaymentValidator.verifyOrderNo(orderNo);

        assertTrue(result);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        "SHORT123",           // 太短
        "lowercase1234567890123456",  // 小写字母
        "ORDER-1234567890123", // 包含连字符
        "ORDER 1234567890123", // 包含空格
        "SPECIAL@12345678901"  // 包含特殊字符
    })
    @DisplayName("验证非法订单号")
    void testVerifyOrderNo_Invalid(String orderNo) {
        boolean result = PaymentValidator.verifyOrderNo(orderNo);

        assertFalse(result);
    }

    // ==================== 支付状态验证测试 ====================

    @ParameterizedTest
    @ValueSource(strings = {
        "SUCCESS",
        "REFUND",
        "NOTPAY",
        "CLOSED",
        "REVOKED",
        "USERPAYING",
        "PAYERROR"
    })
    @DisplayName("验证合法支付状态")
    void testVerifyPaymentStatus_Valid(String status) {
        boolean result = PaymentValidator.verifyPaymentStatus(status);

        assertTrue(result);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        "PENDING",
        "FAILED",
        "CANCELLED",
        "UNKNOWN",
        "success",  // 小写
        "Success"   // 混合大小写
    })
    @DisplayName("验证非法支付状态")
    void testVerifyPaymentStatus_Invalid(String status) {
        boolean result = PaymentValidator.verifyPaymentStatus(status);

        assertFalse(result);
    }

    // ==================== SHA256 测试 ====================

    @Test
    @DisplayName("验证SHA256加密")
    void testSha256() {
        String data = "test_data";
        String hash = PaymentValidator.sha256(data);

        assertNotNull(hash);
        assertEquals(64, hash.length()); // SHA256输出64个十六进制字符

        // 相同输入应该产生相同输出
        String hash2 = PaymentValidator.sha256(data);
        assertEquals(hash, hash2);

        // 不同输入应该产生不同输出
        String hash3 = PaymentValidator.sha256("different_data");
        assertNotEquals(hash, hash3);
    }

    @Test
    @DisplayName("验证SHA256加密 - 空字符串")
    void testSha256_Empty() {
        String hash = PaymentValidator.sha256("");

        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    // ==================== 随机字符串生成测试 ====================

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10, 16, 32})
    @DisplayName("验证生成随机字符串长度")
    void testGenerateNonceStr_Length(int length) {
        String nonce = PaymentValidator.generateNonceStr(length);

        assertNotNull(nonce);
        assertEquals(length, nonce.length());
    }

    @Test
    @DisplayName("验证生成随机字符串唯一性")
    void testGenerateNonceStr_Uniqueness() {
        String nonce1 = PaymentValidator.generateNonceStr(16);
        String nonce2 = PaymentValidator.generateNonceStr(16);

        assertNotEquals(nonce1, nonce2);
    }

    // ==================== 回调重复验证测试 ====================

    @Test
    @DisplayName("验证首次回调")
    void testIsFirstTimeCallback_FirstTime() {
        boolean result = PaymentValidator.isFirstTimeCallback("order_123", "timestamp_123");

        // 默认实现返回true
        assertTrue(result);
    }
}
