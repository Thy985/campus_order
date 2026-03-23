package com.xingchen.backend.util.payment;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付测试工具类
 * 用于开发环境模拟支付回调和生成测试数据
 *
 * @author 小跃
 * @date 2026-02-14
 */
@Slf4j
public class PaymentTestUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'+08:00'");

    /**
     * 生成测试用的微信支付回调数据
     *
     * @param outTradeNo    商户订单号
     * @param transactionId 微信支付订单号
     * @param totalAmount   订单总金额（分）
     * @param payerOpenid   支付者openid
     * @return 回调数据JSON字符串
     */
    public static String generateWeChatPayCallbackData(String outTradeNo, String transactionId, Integer totalAmount, String payerOpenid) {
        Map<String, Object> callbackData = new HashMap<>();
        
        // 应用ID
        callbackData.put("appid", "test_app_id");
        
        // 商户号
        callbackData.put("mchid", "test_mch_id");
        
        // 商户订单号
        callbackData.put("out_trade_no", outTradeNo);
        
        // 微信支付订单号
        callbackData.put("transaction_id", transactionId);
        
        // 交易类型
        callbackData.put("trade_type", "NATIVE");
        
        // 交易状态
        callbackData.put("trade_state", "SUCCESS");
        
        // 交易状态描述
        callbackData.put("trade_state_desc", "支付成功");
        
        // 付款银行
        callbackData.put("bank_type", "CMC");
        
        // 附加数据
        callbackData.put("attach", "");
        
        // 支付完成时间
        callbackData.put("success_time", LocalDateTime.now().format(FORMATTER));
        
        // 支付者
        Map<String, String> payer = new HashMap<>();
        payer.put("openid", payerOpenid != null ? payerOpenid : "test_openid_" + IdUtil.simpleUUID());
        callbackData.put("payer", payer);
        
        // 订单金额
        Map<String, Object> amount = new HashMap<>();
        amount.put("total", totalAmount);
        amount.put("payer_total", totalAmount);
        amount.put("currency", "CNY");
        amount.put("payer_currency", "CNY");
        callbackData.put("amount", amount);
        
        return JSONUtil.toJsonStr(callbackData);
    }

    /**
     * 生成测试用的支付回调请求头
     *
     * @return 请求头Map
     */
    public static Map<String, String> generateWeChatPayCallbackHeaders() {
        Map<String, String> headers = new HashMap<>();
        
        headers.put("Wechatpay-Timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        headers.put("Wechatpay-Nonce", IdUtil.simpleUUID());
        headers.put("Wechatpay-Signature", "test_signature_" + IdUtil.simpleUUID());
        headers.put("Wechatpay-Serial", "test_serial_number");
        headers.put("Wechatpay-Signature-Type", "WECHATPAY2-SHA256-RSA2048");
        
        return headers;
    }

    /**
     * 模拟支付成功回调
     *
     * @param orderNo 订单号
     * @param amount  金额(元)
     * @return 回调数据
     */
    public static String simulatePaymentSuccessCallback(String orderNo, BigDecimal amount) {
        String transactionId = "WX" + System.currentTimeMillis();
        Integer totalAmount = amount.multiply(BigDecimal.valueOf(100)).intValue();
        
        String callbackData = generateWeChatPayCallbackData(orderNo, transactionId, totalAmount, null);
        
        log.info("========== 模拟支付成功回调 ==========");
        log.info("订单号: {}", orderNo);
        log.info("微信支付单号: {}", transactionId);
        log.info("支付金额: {} 元", amount);
        log.info("回调数据: {}", callbackData);
        log.info("========================================");
        
        return callbackData;
    }

    /**
     * 模拟支付失败回调
     *
     * @param orderNo 订单号
     * @param reason  失败原因
     * @return 回调数据
     */
    public static String simulatePaymentFailureCallback(String orderNo, String reason) {
        Map<String, Object> callbackData = new HashMap<>();
        
        callbackData.put("appid", "test_app_id");
        callbackData.put("mchid", "test_mch_id");
        callbackData.put("out_trade_no", orderNo);
        callbackData.put("transaction_id", "WX" + System.currentTimeMillis());
        callbackData.put("trade_type", "NATIVE");
        callbackData.put("trade_state", "PAYERROR");
        callbackData.put("trade_state_desc", reason);
        
        String result = JSONUtil.toJsonStr(callbackData);
        
        log.info("========== 模拟支付失败回调 ==========");
        log.info("订单号: {}", orderNo);
        log.info("失败原因: {}", reason);
        log.info("回调数据: {}", result);
        log.info("========================================");
        
        return result;
    }

    /**
     * 打印支付参数(用于调试)
     *
     * @param title  标题
     * @param params 参数Map
     */
    public static void printPaymentParams(String title, Map<String, ?> params) {
        log.info("========== {} ==========", title);
        params.forEach((key, value) -> log.info("{}: {}", key, value));
        log.info("========================================");
    }

    /**
     * 生成测试用的支付单号
     *
     * @return 支付单号
     */
    public static String generateTestPaymentNo() {
        return "TEST_PAY_" + System.currentTimeMillis() + "_" + IdUtil.randomUUID().substring(0, 8);
    }

    /**
     * 生成测试用的退款单号
     *
     * @return 退款单号
     */
    public static String generateTestRefundNo() {
        return "TEST_REFUND_" + System.currentTimeMillis() + "_" + IdUtil.randomUUID().substring(0, 8);
    }

    /**
     * 生成测试用的微信支付订单号
     *
     * @return 微信支付订单号
     */
    public static String generateTestTransactionId() {
        return "WX_TEST_" + System.currentTimeMillis();
    }

    /**
     * 验证支付回调数据格式
     *
     * @param callbackData 回调数据JSON字符串
     * @return 是否合法
     */
    public static boolean validateCallbackDataFormat(String callbackData) {
        try {
            JSONObject json = JSONUtil.parseObj(callbackData);
            
            // 检查必需字段
            String[] requiredFields = {
                "appid", "mchid", "out_trade_no", "transaction_id", 
                "trade_state", "success_time", "payer", "amount"
            };
            
            for (String field : requiredFields) {
                if (!json.containsKey(field)) {
                    log.error("回调数据缺少必需字段: {}", field);
                    return false;
                }
            }
            
            // 检查金额字段
            JSONObject amount = json.getJSONObject("amount");
            if (!amount.containsKey("total") || !amount.containsKey("currency")) {
                log.error("金额字段格式不正确");
                return false;
            }
            
            // 检查支付者字段
            JSONObject payer = json.getJSONObject("payer");
            if (!payer.containsKey("openid")) {
                log.error("支付者字段格式不正确");
                return false;
            }
            
            log.info("支付回调数据格式验证通过");
            return true;
            
        } catch (Exception e) {
            log.error("支付回调数据格式验证失败", e);
            return false;
        }
    }

    /**
     * 生成支付二维码URL(测试用)
     *
     * @param paymentNo 支付单号
     * @return 二维码URL
     */
    public static String generateTestQrCodeUrl(String paymentNo) {
        return "weixin://wxpay/bizpayurl?pr=test_" + paymentNo;
    }

    /**
     * 模拟支付延迟
     *
     * @param seconds 延迟秒数
     */
    public static void simulatePaymentDelay(int seconds) {
        try {
            log.info("模拟支付处理延迟 {} 秒...", seconds);
            Thread.sleep(seconds * 1000L);
            log.info("延迟结束");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("延迟被中断", e);
        }
    }
}
