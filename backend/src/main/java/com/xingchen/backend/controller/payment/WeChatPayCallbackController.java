package com.xingchen.backend.controller.payment;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xingchen.backend.service.PaymentService;
import com.xingchen.backend.service.payment.WeChatPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 微信支付回调控制器
 * 
 * 处理微信支付的异步通知
 * 
 * @author 小跃
 * @date 2026-02-14
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/wechat")
@RequiredArgsConstructor
public class WeChatPayCallbackController {

    private final WeChatPayService weChatPayService;
    private final PaymentService paymentService;

    /**
     * 微信支付异步通知
     * 
     * 微信支付完成后，微信服务器会调用此接口通知支付结果
     * 
     * @param serial 微信支付证书序列号
     * @param nonce 随机字符串
     * @param timestamp 时间戳
     * @param signature 签名
     * @param requestBody 请求体
     * @return 响应结果
     */
    @PostMapping("/notify")
    public ResponseEntity<String> wechatNotify(
            @RequestHeader("Wechatpay-Serial") String serial,
            @RequestHeader("Wechatpay-Nonce") String nonce,
            @RequestHeader("Wechatpay-Timestamp") String timestamp,
            @RequestHeader("Wechatpay-Signature") String signature,
            @RequestBody String requestBody) {

        log.info("收到微信支付回调，serial: {}", serial);

        try {
            // 验证签名并处理回调
            boolean success = weChatPayService.handlePaymentCallback(
                    requestBody, signature, serial, timestamp, nonce);

            if (success) {
                // 解析回调数据
                JSONObject jsonObject = JSONUtil.parseObj(requestBody);
                JSONObject resource = jsonObject.getJSONObject("resource");

                if (resource != null) {
                    // 从resource中获取支付信息
                    String outTradeNo = resource.getStr("out_trade_no");
                    String transactionId = resource.getStr("transaction_id");
                    String tradeState = resource.getStr("trade_state");

                    if ("SUCCESS".equals(tradeState)) {
                        // 处理支付成功
                        // 注意：这里需要调用PaymentService的回调处理方法
                        // paymentService.handlePaymentCallback(...);
                        log.info("微信支付成功处理完成: outTradeNo={}, transactionId={}", outTradeNo, transactionId);
                    } else {
                        log.warn("微信支付状态非成功: tradeState={}, outTradeNo={}", tradeState, outTradeNo);
                    }
                }

                // 返回成功响应
                return ResponseEntity.ok("{\"code\": \"SUCCESS\", \"message\": \"成功\"}");
            } else {
                log.error("微信支付回调签名验证失败");
                return ResponseEntity.status(401)
                        .body("{\"code\": \"FAIL\", \"message\": \"签名验证失败\"}");
            }
        } catch (Exception e) {
            log.error("微信支付回调处理异常", e);
            return ResponseEntity.status(500)
                    .body("{\"code\": \"FAIL\", \"message\": \"处理异常\"}");
        }
    }

    /**
     * 微信退款异步通知
     * 
     * 微信退款完成后，微信服务器会调用此接口通知退款结果
     * 
     * @param serial 微信支付证书序列号
     * @param nonce 随机字符串
     * @param timestamp 时间戳
     * @param signature 签名
     * @param requestBody 请求体
     * @return 响应结果
     */
    @PostMapping("/refund/notify")
    public ResponseEntity<String> wechatRefundNotify(
            @RequestHeader("Wechatpay-Serial") String serial,
            @RequestHeader("Wechatpay-Nonce") String nonce,
            @RequestHeader("Wechatpay-Timestamp") String timestamp,
            @RequestHeader("Wechatpay-Signature") String signature,
            @RequestBody String requestBody) {

        log.info("收到微信退款回调，serial: {}", serial);

        try {
            // 验证签名
            boolean success = weChatPayService.handlePaymentCallback(
                    requestBody, signature, serial, timestamp, nonce);

            if (success) {
                // 解析回调数据
                JSONObject jsonObject = JSONUtil.parseObj(requestBody);
                JSONObject resource = jsonObject.getJSONObject("resource");

                if (resource != null) {
                    String outRefundNo = resource.getStr("out_refund_no");
                    String refundStatus = resource.getStr("refund_status");

                    if ("SUCCESS".equals(refundStatus)) {
                        log.info("微信退款成功: outRefundNo={}", outRefundNo);
                        // 可以在这里添加退款成功的业务处理
                    } else {
                        log.warn("微信退款状态非成功: refundStatus={}, outRefundNo={}", refundStatus, outRefundNo);
                    }
                }

                // 返回成功响应
                return ResponseEntity.ok("{\"code\": \"SUCCESS\", \"message\": \"成功\"}");
            } else {
                log.error("微信退款回调签名验证失败");
                return ResponseEntity.status(401)
                        .body("{\"code\": \"FAIL\", \"message\": \"签名验证失败\"}");
            }
        } catch (Exception e) {
            log.error("微信退款回调处理异常", e);
            return ResponseEntity.status(500)
                    .body("{\"code\": \"FAIL\", \"message\": \"处理异常\"}");
        }
    }
}
