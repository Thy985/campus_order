package com.xingchen.backend.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.xingchen.backend.config.payment.AlipayProperties;
import com.xingchen.backend.entity.Order;
import com.xingchen.backend.entity.Payment;
import com.xingchen.backend.mapper.OrderMapper;
import com.xingchen.backend.mapper.PaymentMapper;
import com.xingchen.backend.service.OrderService;
import com.xingchen.backend.service.payment.AlipayService;
import com.xingchen.backend.util.constant.Constants;
import com.xingchen.backend.util.idempotent.IdempotentUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 支付宝支付服务实现类
 * 
 * 包含完整的幂等性保护：
 * 1. 分布式锁防止并发重复处理
 * 2. 数据库乐观锁防止状态重复更新
 * 3. Redis标记防止重复回调
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlipayServiceImpl implements AlipayService {

    private final AlipayClient alipayClient;
    private final AlipayProperties alipayProperties;
    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final OrderService orderService;
    private final IdempotentUtil idempotentUtil;

    @Override
    public String createPcPayment(Order order, String paymentNo) {
        try {
            log.info("创建支付宝网页支付，orderNo: {}, paymentNo: {}, amount: {}", 
                    order.getOrderNo(), paymentNo, order.getActualAmount());
            log.info("支付宝回调地址 - notifyUrl: {}, returnUrl: {}", 
                    alipayProperties.getNotifyUrl(), alipayProperties.getReturnUrl());
            
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(alipayProperties.getNotifyUrl());
            request.setReturnUrl(alipayProperties.getReturnUrl());
            
            AlipayTradePagePayModel model = new AlipayTradePagePayModel();
            model.setOutTradeNo(paymentNo);
            model.setTotalAmount(order.getActualAmount().toString());
            model.setSubject("校园外卖订单-" + order.getOrderNo());
            model.setProductCode("FAST_INSTANT_TRADE_PAY");
            
            request.setBizModel(model);
            
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            
            if (response.isSuccess()) {
                log.info("支付宝网页支付创建成功，paymentNo: {}", paymentNo);
                return response.getBody();
            } else {
                log.error("支付宝网页支付创建失败，paymentNo: {}, code: {}, msg: {}", 
                        paymentNo, response.getCode(), response.getMsg());
                throw new RuntimeException("支付宝支付创建失败: " + response.getMsg());
            }
        } catch (AlipayApiException e) {
            log.error("支付宝网页支付创建异常，paymentNo: {}", paymentNo, e);
            throw new RuntimeException("支付宝支付创建异常", e);
        }
    }

    @Override
    public String createQrCodePayment(Order order, String paymentNo) {
        try {
            log.info("创建支付宝扫码支付，orderNo: {}, paymentNo: {}, amount: {}", 
                    order.getOrderNo(), paymentNo, order.getActualAmount());
            
            AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
            request.setNotifyUrl(alipayProperties.getNotifyUrl());
            
            AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
            model.setOutTradeNo(paymentNo);
            model.setTotalAmount(order.getActualAmount().toString());
            model.setSubject("校园外卖订单-" + order.getOrderNo());
            
            request.setBizModel(model);
            
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            
            if (response.isSuccess()) {
                log.info("支付宝扫码支付创建成功，paymentNo: {}, qrCode: {}", 
                        paymentNo, response.getQrCode());
                return response.getQrCode();
            } else {
                log.error("支付宝扫码支付创建失败，paymentNo: {}, code: {}, msg: {}", 
                        paymentNo, response.getCode(), response.getMsg());
                throw new RuntimeException("支付宝扫码支付创建失败: " + response.getMsg());
            }
        } catch (AlipayApiException e) {
            log.error("支付宝扫码支付创建异常，paymentNo: {}", paymentNo, e);
            throw new RuntimeException("支付宝扫码支付创建异常", e);
        }
    }

    @Override
    public String queryPaymentStatus(String paymentNo) {
        try {
            log.info("查询支付宝支付状态，paymentNo: {}", paymentNo);
            
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            model.setOutTradeNo(paymentNo);
            request.setBizModel(model);
            
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            
            if (response.isSuccess()) {
                log.info("支付宝支付查询成功，paymentNo: {}, status: {}", 
                        paymentNo, response.getTradeStatus());
                return response.getTradeStatus();
            } else {
                log.warn("支付宝支付查询失败，paymentNo: {}, code: {}, msg: {}", 
                        paymentNo, response.getCode(), response.getMsg());
                return null;
            }
        } catch (AlipayApiException e) {
            log.error("支付宝支付查询异常，paymentNo: {}", paymentNo, e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleNotify(String paymentNo, String tradeNo, String tradeStatus,
                             BigDecimal amount) {
        log.info("处理支付宝回调，paymentNo: {}, tradeNo: {}, status: {}",
                paymentNo, tradeNo, tradeStatus);

        // ========== 第一层防护：分布式锁 ==========
        IdempotentUtil.IdempotentLock lock = idempotentUtil.tryLock("alipay:notify:" + paymentNo, 60);
        if (lock == null) {
            log.warn("支付宝回调正在处理中，跳过重复请求: {}", paymentNo);
            return;
        }

        try {
            // ========== 第二层防护：Redis标记 ==========
            String processedKey = "alipay:processed:" + paymentNo;
            if (!idempotentUtil.checkAndMark(processedKey, 86400)) { // 24小时过期
                log.warn("支付宝回调已处理，跳过重复请求: {}", paymentNo);
                return;
            }

            // 查询支付记录
            Payment payment = paymentMapper.selectByPaymentNo(paymentNo);
            if (payment == null) {
                log.error("支付记录不存在，paymentNo: {}", paymentNo);
                return;
            }

            // 打印支付记录详情用于调试
            log.info("查询到支付记录: id={}, orderId={}, orderNo={}, status={}, version={}",
                    payment.getId(), payment.getOrderId(), payment.getOrderNo(),
                    payment.getStatus(), payment.getVersion());

            // ========== 第三层防护：数据库状态检查 ==========
            if (payment.getStatus() == Constants.PaymentStatus.PAID) {
                log.info("支付记录已处理，paymentNo: {}", paymentNo);
                return;
            }

            // 验证支付金额
            if (amount.compareTo(payment.getAmount()) != 0) {
                log.error("支付金额不匹配，paymentNo: {}，期望: {}，实际: {}",
                        paymentNo, payment.getAmount(), amount);
                // 金额不匹配，清除标记，允许后续正确处理
                idempotentUtil.clearMark(processedKey);
                throw new RuntimeException("支付金额不匹配，期望: " + payment.getAmount() + ", 实际: " + amount);
            }

            // 判断交易状态
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // ========== 第四层防护：乐观锁更新 ==========
                LocalDateTime now = LocalDateTime.now();
                int updated = paymentMapper.updateStatusWithVersion(
                        payment.getId(), 
                        Constants.PaymentStatus.PAID, 
                        now, 
                        tradeNo,
                        now, // callback_time
                        payment.getVersion()
                );

                if (updated == 0) {
                    log.warn("支付状态已被其他线程更新，paymentNo: {}", paymentNo);
                    return;
                }

                // 更新订单状态
                orderService.paySuccess(payment.getOrderId());

                // 标记处理完成（延长过期时间到7天）
                idempotentUtil.markCompleted(processedKey, 604800);

                log.info("支付成功处理完成，paymentNo: {}, orderId: {}",
                        paymentNo, payment.getOrderId());
            } else if ("TRADE_CLOSED".equals(tradeStatus)) {
                // 交易关闭
                paymentMapper.closePayment(payment.getId(), Constants.PaymentStatus.CLOSED);

                log.info("支付已关闭，paymentNo: {}", paymentNo);
            }
        } catch (Exception e) {
            // 发生异常，清除标记，允许重试
            idempotentUtil.clearMark("alipay:processed:" + paymentNo);
            log.error("处理支付宝回调异常，paymentNo: {}", paymentNo, e);
            throw e;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean verifyNotify(Map<String, String> params) {
        try {
            // 清洗 sign 字段中的空白字符和全角字符（根据博客经验）
            String rawSign = params.get("sign");
            if (rawSign != null) {
                String cleanedSign = rawSign.replaceAll("\\s+", "").replaceAll("\u3000", "");
                params.put("sign", cleanedSign);
            }

            // 判断是同步通知还是异步通知
            boolean isAsyncNotify = params.containsKey("notify_id") || params.containsKey("notify_time");
            log.info("是否为异步通知: {}", isAsyncNotify);

            // 打印所有参数用于调试
            log.info("========== 支付宝回调参数 ==========");
            params.forEach((key, value) -> {
                if (!"sign".equals(key)) {
                    log.info("  {}: {}", key, value);
                } else {
                    log.info("  {}: {}...", key, value != null ? value.substring(0, Math.min(30, value.length())) : "null");
                }
            });
            log.info("====================================");

            // 沙箱环境临时跳过签名验证（用于调试）
            if (Boolean.TRUE.equals(alipayProperties.getSandbox())) {
                log.warn("【沙箱环境】临时跳过签名验证，直接返回true");
                return true;
            }

            // 处理支付宝公钥格式，确保有正确的头尾
            String publicKey = alipayProperties.getPublicKey();
            if (publicKey == null || publicKey.trim().isEmpty()) {
                log.error("支付宝公钥未配置");
                return false;
            }

            // 确保公钥有正确的格式
            if (!publicKey.startsWith("-----BEGIN")) {
                publicKey = "-----BEGIN PUBLIC KEY-----\n" + publicKey + "\n-----END PUBLIC KEY-----";
            }

            log.info("支付宝签名验证参数 - sign: {}, publicKey前50字符: {}",
                    params.get("sign") != null ? params.get("sign").substring(0, Math.min(50, params.get("sign").length())) + "..." : "null",
                    publicKey.substring(0, Math.min(50, publicKey.length())));

            // 使用支付宝SDK验证签名（自动排除sign和sign_type字段）
            // 注意：rsaCheckV1 适用于异步通知，rsaCheckV2 适用于同步通知
            boolean result;
            if (isAsyncNotify) {
                // 异步通知使用 rsaCheckV1
                log.info("使用 rsaCheckV1 验证异步通知签名");
                result = AlipaySignature.rsaCheckV1(params, publicKey, "UTF-8", "RSA2");
            } else {
                // 同步通知使用 rsaCheckV2
                log.info("使用 rsaCheckV2 验证同步通知签名");
                result = AlipaySignature.rsaCheckV2(params, publicKey, "UTF-8", "RSA2");
            }
            log.info("支付宝签名验证结果: {}", result);
            return result;
        } catch (AlipayApiException e) {
            log.error("支付宝签名验证异常: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void simulatedPaySuccess(Long orderId, Long userId) {
        log.info("模拟支付成功，orderId: {}, userId: {}", orderId, userId);

        // 查询订单
        Order order = orderMapper.selectOneById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 验证订单归属
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }

        // 验证订单状态
        if (order.getStatus() != Constants.OrderStatus.WAIT_PAY) {
            throw new RuntimeException("订单状态不正确");
        }

        // 查询或创建支付记录
        Payment payment = paymentMapper.selectByOrderId(orderId);
        if (payment == null) {
            // 创建支付记录
            payment = new Payment();
            payment.setOrderNo(order.getOrderNo());
            payment.setOrderId(order.getId());
            payment.setPaymentNo(generatePaymentNo());
            payment.setUserId(order.getUserId());
            payment.setAmount(order.getActualAmount());
            payment.setChannel(Constants.PaymentChannel.ALIPAY);
            payment.setStatus(Constants.PaymentStatus.UNPAID);
            payment.setVersion(0);
            payment.setCreateTime(LocalDateTime.now());
            payment.setUpdateTime(LocalDateTime.now());
            paymentMapper.insert(payment);
        }

        // 更新支付状态为已支付
        LocalDateTime now = LocalDateTime.now();
        paymentMapper.updateStatusWithVersion(
                payment.getId(),
                Constants.PaymentStatus.PAID,
                now,
                "SIMULATED_" + System.currentTimeMillis(),
                now,
                payment.getVersion()
        );

        // 更新订单状态
        orderService.paySuccess(orderId);

        log.info("模拟支付成功处理完成，orderId: {}, paymentNo: {}", orderId, payment.getPaymentNo());
    }

    private String generatePaymentNo() {
        return "PAY" + System.currentTimeMillis();
    }
}
