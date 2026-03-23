package com.xingchen.backend.service.payment;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付宝支付服务接口
 */
public interface AlipayService {

    /**
     * 创建支付宝电脑网站支付
     *
     * @param order     订单信息
     * @param paymentNo 支付单号
     * @return 支付表单HTML
     */
    String createPcPayment(com.xingchen.backend.entity.Order order, String paymentNo);

    /**
     * 创建支付宝扫码支付（二维码支付）
     * 用户使用支付宝APP扫码完成支付
     *
     * @param order     订单信息
     * @param paymentNo 支付单号
     * @return 支付二维码链接（可转换为二维码图片）
     */
    String createQrCodePayment(com.xingchen.backend.entity.Order order, String paymentNo);

    /**
     * 查询支付宝订单状态
     *
     * @param paymentNo 支付单号
     * @return 支付状态
     */
    String queryPaymentStatus(String paymentNo);

    /**
     * 处理支付宝异步通知
     *
     * @param paymentNo   支付单号
     * @param tradeNo     支付宝交易号
     * @param tradeStatus 交易状态     * @param amount      支付金额
     */
    void handleNotify(String paymentNo, String tradeNo, String tradeStatus, BigDecimal amount);

    /**
     * 验证支付宝签名
     *
     * @param params 通知参数
     * @return 是否验证通过
     */
    boolean verifyNotify(Map<String, String> params);

    /**
     * 处理模拟支付成功（仅用于测试）
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     */
    void simulatedPaySuccess(Long orderId, Long userId);
}
