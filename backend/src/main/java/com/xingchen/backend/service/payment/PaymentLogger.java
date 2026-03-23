package com.xingchen.backend.service.payment;

import com.xingchen.backend.entity.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 支付日志记录器
 * 用于统一记录支付相关操作日志
 *
 * @author 小跃
 * @date 2026-02-14
 */
@Slf4j
@Component
public class PaymentLogger {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 记录支付创建日志
     */
    public void logPaymentCreation(Payment payment, String paymentType) {
        log.info("========== 创建支付订单 ==========");
        log.info("支付类型: {}", paymentType);
        log.info("支付单号: {}", payment.getPaymentNo());
        log.info("订单号: {}", payment.getOrderNo());
        log.info("支付金额: {} 元", payment.getAmount());
        log.info("用户ID: {}", payment.getUserId());
        log.info("创建时间: {}", LocalDateTime.now().format(FORMATTER));
        log.info("==================================");
    }

    /**
     * 记录支付成功日志
     */
    public void logPaymentSuccess(String paymentNo, String transactionId, String orderNo) {
        log.info("========== 支付成功 ==========");
        log.info("支付单号: {}", paymentNo);
        log.info("微信支付单号: {}", transactionId);
        log.info("订单号: {}", orderNo);
        log.info("完成时间: {}", LocalDateTime.now().format(FORMATTER));
        log.info("==============================");
    }

    /**
     * 记录支付失败日志
     */
    public void logPaymentFailure(String paymentNo, String reason) {
        log.error("========== 支付失败 ==========");
        log.error("支付单号: {}", paymentNo);
        log.error("失败原因: {}", reason);
        log.error("失败时间: {}", LocalDateTime.now().format(FORMATTER));
        log.error("==============================");
    }

    /**
     * 记录支付回调日志
     */
    public void logPaymentCallback(String requestBody, String signature, String timestamp, String nonce) {
        log.info("========== 接收支付回调 ==========");
        log.info("回调时间: {}", LocalDateTime.now().format(FORMATTER));
        log.info("时间戳: {}", timestamp);
        log.info("随机数: {}", nonce);
        log.info("签名: {}", signature.substring(0, Math.min(20, signature.length())) + "...");
        log.info("请求体长度: {} 字节", requestBody.length());
        log.info("==================================");
    }

    /**
     * 记录支付查询日志
     */
    public void logPaymentQuery(String paymentNo, String status) {
        log.info("========== 查询支付订单 ==========");
        log.info("支付单号: {}", paymentNo);
        log.info("支付状态: {}", status);
        log.info("查询时间: {}", LocalDateTime.now().format(FORMATTER));
        log.info("==================================");
    }

    /**
     * 记录支付关闭日志
     */
    public void logPaymentClose(String paymentNo, boolean success) {
        log.info("========== 关闭支付订单 ==========");
        log.info("支付单号: {}", paymentNo);
        log.info("关闭结果: {}", success ? "成功" : "失败");
        log.info("关闭时间: {}", LocalDateTime.now().format(FORMATTER));
        log.info("==================================");
    }

    /**
     * 记录退款申请日志
     */
    public void logRefundRequest(String paymentNo, String refundNo, Long refundAmount, String reason) {
        log.info("========== 申请退款 ==========");
        log.info("支付单号: {}", paymentNo);
        log.info("退款单号: {}", refundNo);
        log.info("退款金额: {} 元", refundAmount);
        log.info("退款原因: {}", reason);
        log.info("申请时间: {}", LocalDateTime.now().format(FORMATTER));
        log.info("==============================");
    }

    /**
     * 记录退款成功日志
     */
    public void logRefundSuccess(String refundNo, String refundId) {
        log.info("========== 退款成功 ==========");
        log.info("退款单号: {}", refundNo);
        log.info("微信退款单号: {}", refundId);
        log.info("完成时间: {}", LocalDateTime.now().format(FORMATTER));
        log.info("==============================");
    }

    /**
     * 记录退款失败日志
     */
    public void logRefundFailure(String refundNo, String reason) {
        log.error("========== 退款失败 ==========");
        log.error("退款单号: {}", refundNo);
        log.error("失败原因: {}", reason);
        log.error("失败时间: {}", LocalDateTime.now().format(FORMATTER));
        log.error("==============================");
    }

    /**
     * 记录支付异常日志
     */
    public void logPaymentException(String operation, String paymentNo, Exception e) {
        log.error("========== 支付异常 ==========");
        log.error("操作类型: {}", operation);
        log.error("支付单号: {}", paymentNo);
        log.error("异常信息: {}", e.getMessage());
        log.error("异常时间: {}", LocalDateTime.now().format(FORMATTER));
        log.error("==============================", e);
    }
}
