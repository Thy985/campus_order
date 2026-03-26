package com.xingchen.backend.service.impl;

import cn.hutool.core.util.IdUtil;
import com.xingchen.backend.entity.Order;
import com.xingchen.backend.entity.Payment;
import com.xingchen.backend.mapper.OrderMapper;
import com.xingchen.backend.mapper.PaymentMapper;
import com.xingchen.backend.event.OrderPaidEvent;
import com.xingchen.backend.service.OrderService;
import com.xingchen.backend.service.PaymentService;
import org.springframework.context.ApplicationEventPublisher;
import com.xingchen.backend.dto.request.payment.PaymentCallbackRequest;
import com.xingchen.backend.dto.request.payment.PaymentRequest;
import com.xingchen.backend.dto.response.payment.PaymentResponse;
import com.xingchen.backend.service.payment.AlipayService;
import com.xingchen.backend.util.constant.Constants;
import com.xingchen.backend.util.constant.ErrorCode;
import com.xingchen.backend.exception.BusinessException;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付服务实现 - 仅支持支付宝沙箱支付
 *
 * @author xingchen
 * @date 2026-02-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final OrderService orderService;
    private final AlipayService alipayService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResponse generatePaymentUrl(PaymentRequest request) {
        // 查询订单
        Order order = orderMapper.selectOneByQuery(
                QueryWrapper.create()
                        .where("order_no = ?", request.getOrderNo())
        );

        if (order == null || order.getIsDeleted() == Constants.DeleteFlag.DELETED) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXIST, "订单不存在");
        }

        // 验证订单状态
        if (order.getStatus() != Constants.OrderStatus.WAIT_PAY) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单状态错误，无法支付");
        }

        // 验证支付金额
        if (request.getAmount().compareTo(order.getActualAmount()) != 0) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_ERROR, "支付金额不匹配");
        }

        // 检查是否已有支付记录
        Payment existPayment = paymentMapper.selectByOrderId(order.getId());
        if (existPayment != null && existPayment.getStatus() == Constants.PaymentStatus.PAID) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单已支付");
        }

        // 生成支付单号
        String paymentNo = generatePaymentNo();

        // 创建支付记录
        Payment payment = new Payment();
        payment.setPaymentNo(paymentNo);
        payment.setOrderId(order.getId());
        payment.setOrderNo(order.getOrderNo());
        payment.setUserId(order.getUserId());
        payment.setAmount(request.getAmount());
        payment.setChannel(Constants.PaymentChannel.ALIPAY); // 固定为支付宝
        payment.setStatus(Constants.PaymentStatus.UNPAID);
        payment.setVersion(0); // 设置乐观锁初始版本号
        payment.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        payment.setCreateTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());

        paymentMapper.insert(payment);

        // 创建支付宝支付
        String paymentForm = alipayService.createPcPayment(order, paymentNo);

        // 构建支付响应
        Map<String, String> paymentParams = new HashMap<>();
        paymentParams.put("paymentForm", paymentForm);
        paymentParams.put("paymentNo", paymentNo);

        PaymentResponse response = new PaymentResponse();
        response.setOrderNo(order.getOrderNo());
        response.setStatus(Constants.PaymentStatus.UNPAID);
        response.setPaymentMethod(Constants.PaymentChannel.ALIPAY);
        response.setPaymentParams(paymentParams);

        log.info("支付宝支付单创建成功: paymentNo={}, orderId={}, amount={}",
                paymentNo, order.getId(), request.getAmount());

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePaymentCallback(PaymentCallbackRequest request) {
        // 查询支付记录
        Payment payment = paymentMapper.selectByPaymentNo(request.getPaymentNo());

        if (payment == null) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_EXIST, "支付记录不存在");
        }

        // 验证支付状态
        if (payment.getStatus() == Constants.PaymentStatus.PAID) {
            log.warn("支付已完成，忽略回调: paymentNo={}", request.getPaymentNo());
            return;
        }

        // 验证支付金额
        if (request.getAmount().compareTo(payment.getAmount()) != 0) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_ERROR, "支付金额不匹配");
        }

        // 更新支付记录
        paymentMapper.updateStatus(payment.getId(), Constants.PaymentStatus.PAID, LocalDateTime.now(), request.getTradeNo());

        // 更新订单状态
        orderService.paySuccess(payment.getOrderId());

        log.info("支付回调处理成功: paymentNo={}, orderId={}, tradeNo={}",
                request.getPaymentNo(), payment.getOrderId(), request.getTradeNo());
    }

    @Override
    public Integer queryPaymentStatus(String orderNo) {
        Payment payment = paymentMapper.selectByOrderNo(orderNo);
        if (payment == null) {
            return null;
        }
        return payment.getStatus();
    }

    @Override
    public void closePayment(String orderNo) {
        Payment payment = paymentMapper.selectByOrderNo(orderNo);
        if (payment == null) {
            return;
        }

        // 只有待支付状态才能关闭
        if (payment.getStatus() != Constants.PaymentStatus.UNPAID) {
            return;
        }

        paymentMapper.closePayment(payment.getId(), Constants.PaymentStatus.CLOSED);

        log.info("支付单已关闭: paymentNo={}", payment.getPaymentNo());
    }

    /**
     * 生成支付单号
     */
    private String generatePaymentNo() {
        return "PAY" + IdUtil.getSnowflakeNextIdStr();
    }

    @Override
    public Payment getPaymentByOrderNo(String orderNo) {
        return paymentMapper.selectByOrderNo(orderNo);
    }
}
