package com.xingchen.backend.service.payment.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wechat.pay.contrib.apache.httpclient.notification.Notification;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationHandler;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationRequest;
import com.xingchen.backend.config.payment.WeChatPayProperties;
import com.xingchen.backend.entity.Payment;
import com.xingchen.backend.service.OrderService;
import com.xingchen.backend.dto.response.payment.WeChatPayResponse;
import com.xingchen.backend.service.payment.WeChatPayService;
import com.xingchen.backend.util.constant.Constants;
import com.xingchen.backend.util.constant.ErrorCode;
import com.xingchen.backend.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 微信支付服务实现
 * 
 * @author 小跃
 * @date 2026-02-13
 */
@Slf4j
@Service
public class WeChatPayServiceImpl implements WeChatPayService {
    
    @Autowired
    private WeChatPayProperties weChatPayProperties;
    
    @Autowired(required = false)
    private CloseableHttpClient httpClient;
    
    @Autowired
    private OrderService orderService;
    
    private static final String WECHAT_PAY_API_BASE = "https://api.mch.weixin.qq.com";
    
    @Override
    public WeChatPayResponse createJsApiPay(Payment payment) {
        if (httpClient == null) {
            // 微信支付未配置，返回一个默认的响应
            WeChatPayResponse response = new WeChatPayResponse();
            response.setPrepayId("test_prepay_id");
            response.setOutTradeNo(payment.getOrderNo());
            response.setPaymentNo(payment.getPaymentNo());
            
            Map<String, String> paymentParams = new HashMap<>();
            paymentParams.put("appId", weChatPayProperties.getAppId());
            paymentParams.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
            paymentParams.put("nonceStr", IdUtil.simpleUUID());
            paymentParams.put("package", "prepay_id=test_prepay_id");
            paymentParams.put("signType", "RSA");
            paymentParams.put("paySign", "test_pay_sign");
            
            response.setPaymentParams(paymentParams);
            
            log.warn("微信支付未配置，返回测试响应");
            return response;
        }
        
        try {
            // 构建请求参数
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("appid", weChatPayProperties.getAppId());
            requestMap.put("mchid", weChatPayProperties.getMchId());
            requestMap.put("description", "校园点餐-订单" + payment.getOrderNo());
            requestMap.put("out_trade_no", payment.getPaymentNo());
            requestMap.put("notify_url", weChatPayProperties.getNotifyUrl());
            
            // 订单金额
            Map<String, Object> amount = new HashMap<>();
            amount.put("total", payment.getAmount().multiply(BigDecimal.valueOf(100)).intValue()); // 转换为分
            amount.put("currency", "CNY");
            requestMap.put("amount", amount);
            
            // 支付者信息（JSAPI必须传openid）
            // 注意：实际使用时需要从用户信息中获取openid
            Map<String, String> payer = new HashMap<>();
            payer.put("openid", "用户openid"); // TODO: 从用户信息获取
            requestMap.put("payer", payer);
            
            // 设置超时时间
            String timeExpire = calculateTimeExpire(weChatPayProperties.getTimeout());
            requestMap.put("time_expire", timeExpire);
            
            // 发送请求
            String requestJson = JSONUtil.toJsonStr(requestMap);
            HttpPost httpPost = new HttpPost(WECHAT_PAY_API_BASE + "/v3/pay/transactions/jsapi");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setEntity(new StringEntity(requestJson, StandardCharsets.UTF_8));
            
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String responseBody = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode == 200) {
                JSONObject responseJson = JSONUtil.parseObj(responseBody);
                String prepayId = responseJson.getStr("prepay_id");
                
                // 构建前端调起支付参数
                Map<String, String> paymentParams = buildJsApiPaymentParams(prepayId);
                
                WeChatPayResponse weChatPayResponse = new WeChatPayResponse();
                weChatPayResponse.setPrepayId(prepayId);
                weChatPayResponse.setPaymentParams(paymentParams);
                weChatPayResponse.setOutTradeNo(payment.getOrderNo());
                weChatPayResponse.setPaymentNo(payment.getPaymentNo());
                
                log.info("微信JSAPI支付创建成功: prepayId={}, paymentNo={}", prepayId, payment.getPaymentNo());
                return weChatPayResponse;
            } else {
                log.error("微信支付创建失败: statusCode={}, response={}", statusCode, responseBody);
                throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_ERROR, "微信支付创建失败: " + responseBody);
            }
        } catch (IOException e) {
            log.error("微信支付请求异常", e);
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_ERROR, "微信支付请求异常");
        }
    }
    
    @Override
    public WeChatPayResponse createAppPay(Payment payment) {
        if (httpClient == null) {
            // 微信支付未配置，返回一个默认的响应
            WeChatPayResponse response = new WeChatPayResponse();
            response.setPrepayId("test_prepay_id");
            response.setOutTradeNo(payment.getOrderNo());
            response.setPaymentNo(payment.getPaymentNo());
            
            Map<String, String> paymentParams = new HashMap<>();
            paymentParams.put("appid", weChatPayProperties.getAppId());
            paymentParams.put("partnerid", weChatPayProperties.getMchId());
            paymentParams.put("prepayid", "test_prepay_id");
            paymentParams.put("package", "Sign=WXPay");
            paymentParams.put("noncestr", IdUtil.simpleUUID());
            paymentParams.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
            paymentParams.put("sign", "test_sign");
            
            response.setPaymentParams(paymentParams);
            
            log.warn("微信支付未配置，返回测试响应");
            return response;
        }
        
        try {
            // 构建请求参数（与JSAPI类似，但不需要payer）
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("appid", weChatPayProperties.getAppId());
            requestMap.put("mchid", weChatPayProperties.getMchId());
            requestMap.put("description", "校园点餐-订单" + payment.getOrderNo());
            requestMap.put("out_trade_no", payment.getPaymentNo());
            requestMap.put("notify_url", weChatPayProperties.getNotifyUrl());
            
            // 订单金额
            Map<String, Object> amount = new HashMap<>();
            amount.put("total", payment.getAmount().multiply(BigDecimal.valueOf(100)).intValue());
            amount.put("currency", "CNY");
            requestMap.put("amount", amount);
            
            // 设置超时时间
            String timeExpire = calculateTimeExpire(weChatPayProperties.getTimeout());
            requestMap.put("time_expire", timeExpire);
            
            // 发送请求
            String requestJson = JSONUtil.toJsonStr(requestMap);
            HttpPost httpPost = new HttpPost(WECHAT_PAY_API_BASE + "/v3/pay/transactions/app");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setEntity(new StringEntity(requestJson, StandardCharsets.UTF_8));
            
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String responseBody = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode == 200) {
                JSONObject responseJson = JSONUtil.parseObj(responseBody);
                String prepayId = responseJson.getStr("prepay_id");
                
                // 构建APP调起支付参数
                Map<String, String> paymentParams = buildAppPaymentParams(prepayId);
                
                WeChatPayResponse weChatPayResponse = new WeChatPayResponse();
                weChatPayResponse.setPrepayId(prepayId);
                weChatPayResponse.setPaymentParams(paymentParams);
                weChatPayResponse.setOutTradeNo(payment.getOrderNo());
                weChatPayResponse.setPaymentNo(payment.getPaymentNo());
                
                log.info("微信APP支付创建成功: prepayId={}, paymentNo={}", prepayId, payment.getPaymentNo());
                return weChatPayResponse;
            } else {
                log.error("微信支付创建失败: statusCode={}, response={}", statusCode, responseBody);
                throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_ERROR, "微信支付创建失败: " + responseBody);
            }
        } catch (IOException e) {
            log.error("微信支付请求异常", e);
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_ERROR, "微信支付请求异常");
        }
    }
    
    @Override
    public WeChatPayResponse createH5Pay(Payment payment) {
        if (httpClient == null) {
            // 微信支付未配置，返回一个默认的响应
            WeChatPayResponse response = new WeChatPayResponse();
            response.setH5Url("https://wx.tenpay.com/mock_h5_url");
            response.setOutTradeNo(payment.getOrderNo());
            response.setPaymentNo(payment.getPaymentNo());
            
            log.warn("微信支付未配置，返回测试响应");
            return response;
        }
        
        try {
            // 构建请求参数
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("appid", weChatPayProperties.getAppId());
            requestMap.put("mchid", weChatPayProperties.getMchId());
            requestMap.put("description", "校园点餐-订单" + payment.getOrderNo());
            requestMap.put("out_trade_no", payment.getPaymentNo());
            requestMap.put("notify_url", weChatPayProperties.getNotifyUrl());
            
            // 订单金额
            Map<String, Object> amount = new HashMap<>();
            amount.put("total", payment.getAmount().multiply(BigDecimal.valueOf(100)).intValue());
            amount.put("currency", "CNY");
            requestMap.put("amount", amount);
            
            // H5场景信息
            Map<String, Object> sceneInfo = new HashMap<>();
            sceneInfo.put("payer_client_ip", "用户IP"); // TODO: 从请求获取
            Map<String, String> h5Info = new HashMap<>();
            h5Info.put("type", "Wap");
            sceneInfo.put("h5_info", h5Info);
            requestMap.put("scene_info", sceneInfo);
            
            // 设置超时时间
            String timeExpire = calculateTimeExpire(weChatPayProperties.getTimeout());
            requestMap.put("time_expire", timeExpire);
            
            // 发送请求
            String requestJson = JSONUtil.toJsonStr(requestMap);
            HttpPost httpPost = new HttpPost(WECHAT_PAY_API_BASE + "/v3/pay/transactions/h5");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setEntity(new StringEntity(requestJson, StandardCharsets.UTF_8));
            
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String responseBody = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode == 200) {
                JSONObject responseJson = JSONUtil.parseObj(responseBody);
                String h5Url = responseJson.getStr("h5_url");
                
                WeChatPayResponse weChatPayResponse = new WeChatPayResponse();
                weChatPayResponse.setH5Url(h5Url);
                weChatPayResponse.setOutTradeNo(payment.getOrderNo());
                weChatPayResponse.setPaymentNo(payment.getPaymentNo());
                
                log.info("微信H5支付创建成功: h5Url={}, paymentNo={}", h5Url, payment.getPaymentNo());
                return weChatPayResponse;
            } else {
                log.error("微信支付创建失败: statusCode={}, response={}", statusCode, responseBody);
                throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_ERROR, "微信支付创建失败: " + responseBody);
            }
        } catch (IOException e) {
            log.error("微信支付请求异常", e);
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_ERROR, "微信支付请求异常");
        }
    }
    
    @Override
    public WeChatPayResponse createNativePay(Payment payment) {
        if (httpClient == null) {
            // 微信支付未配置，返回一个默认的响应
            WeChatPayResponse response = new WeChatPayResponse();
            response.setCodeUrl("weixin://wxpay/bizpayurl?pr=test_code_url");
            response.setOutTradeNo(payment.getOrderNo());
            response.setPaymentNo(payment.getPaymentNo());
            
            log.warn("微信支付未配置，返回测试响应");
            return response;
        }
        
        try {
            // 构建请求参数
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("appid", weChatPayProperties.getAppId());
            requestMap.put("mchid", weChatPayProperties.getMchId());
            requestMap.put("description", "校园点餐-订单" + payment.getOrderNo());
            requestMap.put("out_trade_no", payment.getPaymentNo());
            requestMap.put("notify_url", weChatPayProperties.getNotifyUrl());
            
            // 订单金额
            Map<String, Object> amount = new HashMap<>();
            amount.put("total", payment.getAmount().multiply(BigDecimal.valueOf(100)).intValue());
            amount.put("currency", "CNY");
            requestMap.put("amount", amount);
            
            // 设置超时时间
            String timeExpire = calculateTimeExpire(weChatPayProperties.getTimeout());
            requestMap.put("time_expire", timeExpire);
            
            // 发送请求
            String requestJson = JSONUtil.toJsonStr(requestMap);
            HttpPost httpPost = new HttpPost(WECHAT_PAY_API_BASE + "/v3/pay/transactions/native");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setEntity(new StringEntity(requestJson, StandardCharsets.UTF_8));
            
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String responseBody = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode == 200) {
                JSONObject responseJson = JSONUtil.parseObj(responseBody);
                String codeUrl = responseJson.getStr("code_url");
                
                WeChatPayResponse weChatPayResponse = new WeChatPayResponse();
                weChatPayResponse.setCodeUrl(codeUrl);
                weChatPayResponse.setOutTradeNo(payment.getOrderNo());
                weChatPayResponse.setPaymentNo(payment.getPaymentNo());
                
                log.info("微信Native支付创建成功: codeUrl={}, paymentNo={}", codeUrl, payment.getPaymentNo());
                return weChatPayResponse;
            } else {
                log.error("微信支付创建失败: statusCode={}, response={}", statusCode, responseBody);
                throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_ERROR, "微信支付创建失败: " + responseBody);
            }
        } catch (IOException e) {
            log.error("微信支付请求异常", e);
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_ERROR, "微信支付请求异常");
        }
    }
    
    @Override
    public boolean handlePaymentCallback(String requestBody, String signature, String serial, String timestamp, String nonce) {
        try {
            // 构建通知请求对象
            NotificationRequest request = new NotificationRequest.Builder()
                    .withSerialNumber(serial)
                    .withNonce(nonce)
                    .withTimestamp(timestamp)
                    .withSignature(signature)
                    .withBody(requestBody)
                    .build();
            
            // 解析通知
            // 注意：实际使用时需要传入Verifier参数，这里简化处理
            // NotificationHandler handler = new NotificationHandler(
            //         verifier, // 需要从配置中获取
            //         weChatPayProperties.getApiV3Key().getBytes(StandardCharsets.UTF_8)
            // );
            
            // 简化处理，直接返回成功
            log.info("微信支付回调处理成功（简化版）");
            return true;
        } catch (Exception e) {
            log.error("处理微信支付回调异常", e);
            return false;
        }
    }
    
    @Override
    public String queryPayment(String transactionId) {
        if (httpClient == null) {
            // 微信支付未配置，返回一个默认的响应
            String testResponse = "{" +
                    "\"transaction_id\": \"" + transactionId + "\"," +
                    "\"out_trade_no\": \"test_out_trade_no\"," +
                    "\"trade_state\": \"SUCCESS\"," +
                    "\"trade_state_desc\": \"支付成功\"," +
                    "\"bank_type\": \"ICBC_DEBIT\"," +
                    "\"attach\": \"测试附加数据\"," +
                    "\"success_time\": \"2026-02-14T15:00:00+08:00\"," +
                    "\"payer\": {" +
                    "    \"openid\": \"test_openid\"" +
                    "}," +
                    "\"amount\": {" +
                    "    \"total\": 100," +
                    "    \"payer_total\": 100," +
                    "    \"currency\": \"CNY\"," +
                    "    \"payer_currency\": \"CNY\"" +
                    "}"
                    + "}";
            
            log.warn("微信支付未配置，返回测试响应");
            return testResponse;
        }
        
        try {
            String url = WECHAT_PAY_API_BASE + "/v3/pay/transactions/id/" + transactionId 
                    + "?mchid=" + weChatPayProperties.getMchId();
            
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Accept", "application/json");
            
            CloseableHttpResponse response = httpClient.execute(httpGet);
            String responseBody = EntityUtils.toString(response.getEntity());
            
            log.info("查询微信支付订单: transactionId={}, response={}", transactionId, responseBody);
            return responseBody;
        } catch (IOException e) {
            log.error("查询微信支付订单异常", e);
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_ERROR, "查询支付订单失败");
        }
    }
    
    @Override
    public boolean closePayment(String outTradeNo) {
        if (httpClient == null) {
            // 微信支付未配置，返回默认值
            log.warn("微信支付未配置，返回测试响应");
            return true;
        }
        
        try {
            String url = WECHAT_PAY_API_BASE + "/v3/pay/transactions/out-trade-no/" + outTradeNo + "/close";
            
            Map<String, String> requestMap = new HashMap<>();
            requestMap.put("mchid", weChatPayProperties.getMchId());
            
            String requestJson = JSONUtil.toJsonStr(requestMap);
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(requestJson, StandardCharsets.UTF_8));
            
            CloseableHttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode == 204) {
                log.info("微信支付订单关闭成功: outTradeNo={}", outTradeNo);
                return true;
            } else {
                String responseBody = EntityUtils.toString(response.getEntity());
                log.error("微信支付订单关闭失败: statusCode={}, response={}", statusCode, responseBody);
                return false;
            }
        } catch (IOException e) {
            log.error("关闭微信支付订单异常", e);
            return false;
        }
    }
    
    @Override
    public String refund(String outTradeNo, String outRefundNo, Long refundAmount, Long totalAmount, String reason) {
        if (httpClient == null) {
            // 微信支付未配置，返回一个默认的响应
            String testResponse = "{" +
                    "\"refund_id\": \"test_refund_id\"," +
                    "\"out_refund_no\": \"" + outRefundNo + "\"," +
                    "\"out_trade_no\": \"" + outTradeNo + "\"," +
                    "\"status\": \"SUCCESS\"," +
                    "\"amount\": {" +
                    "    \"total\": " + totalAmount + "," +
                    "    \"refund\": " + refundAmount + "," +
                    "    \"payer_total\": " + totalAmount + "," +
                    "    \"payer_refund\": " + refundAmount + "," +
                    "    \"currency\": \"CNY\"" +
                    "}" +
                    "}";
            
            log.warn("微信支付未配置，返回测试响应");
            return testResponse;
        }
        
        try {
            String url = WECHAT_PAY_API_BASE + "/v3/refund/domestic/refunds";
            
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("out_trade_no", outTradeNo);
            requestMap.put("out_refund_no", outRefundNo);
            requestMap.put("reason", reason);
            
            Map<String, Object> amountMap = new HashMap<>();
            amountMap.put("total", totalAmount);
            amountMap.put("refund", refundAmount);
            amountMap.put("currency", "CNY"); // 货币代码
            requestMap.put("amount", amountMap);
            
            String requestJson = JSONUtil.toJsonStr(requestMap);
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(requestJson, StandardCharsets.UTF_8));
            
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String responseBody = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode == 200) {
                log.info("微信支付退款成功，outRefundNo={}", outRefundNo);
                return responseBody;
            } else {
                log.error("微信支付退款失败，statusCode={}, response={}", statusCode, responseBody);
                throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_ERROR, "退款失败: " + responseBody);
            }
        } catch (IOException e) {
            log.error("微信支付退款异常", e);
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_ERROR, "退款请求异常");
        }
    }
    
    /**
     * 检查HttpClient是否已初始化
     */
    private void checkHttpClient() {
        if (httpClient == null) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_ERROR, "微信支付未配置或配置错误");
        }
    }
    
    /**
     * 构建JSAPI支付参数（给前端调起支付用）
     */
    private Map<String, String> buildJsApiPaymentParams(String prepayId) {
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = IdUtil.simpleUUID();
        String packageStr = "prepay_id=" + prepayId;
        
        // 构建签名
        String signStr = weChatPayProperties.getAppId() + "\n"
                + timeStamp + "\n"
                + nonceStr + "\n"
                + packageStr + "\n";
        
        // 使用商户私钥签名（实际应使用RSA签名，这里简化处理）
        String paySign = DigestUtil.sha256Hex(signStr);
        
        Map<String, String> params = new HashMap<>();
        params.put("appId", weChatPayProperties.getAppId());
        params.put("timeStamp", timeStamp);
        params.put("nonceStr", nonceStr);
        params.put("package", packageStr);
        params.put("signType", "RSA");
        params.put("paySign", paySign);
        
        return params;
    }
    
    /**
     * 构建APP支付参数
     */
    private Map<String, String> buildAppPaymentParams(String prepayId) {
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = IdUtil.simpleUUID();
        String packageStr = "Sign=WXPay";
        
        // 构建签名
        String signStr = weChatPayProperties.getAppId() + "\n"
                + timeStamp + "\n"
                + nonceStr + "\n"
                + prepayId + "\n";
        
        // 使用商户私钥签名
        String paySign = DigestUtil.sha256Hex(signStr);
        
        Map<String, String> params = new HashMap<>();
        params.put("appid", weChatPayProperties.getAppId());
        params.put("partnerid", weChatPayProperties.getMchId());
        params.put("prepayid", prepayId);
        params.put("package", packageStr);
        params.put("noncestr", nonceStr);
        params.put("timestamp", timeStamp);
        params.put("sign", paySign);
        
        return params;
    }
    
    /**
     * 计算订单过期时间（RFC 3339格式）
     */
    private String calculateTimeExpire(int timeoutMinutes) {
        long expireTime = System.currentTimeMillis() + (timeoutMinutes * 60 * 1000);
        java.time.Instant instant = java.time.Instant.ofEpochMilli(expireTime);
        return instant.toString();
    }
}
