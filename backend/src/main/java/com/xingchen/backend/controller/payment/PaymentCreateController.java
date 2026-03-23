package com.xingchen.backend.controller.payment;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.xingchen.backend.entity.Order;
import com.xingchen.backend.entity.Payment;
import com.xingchen.backend.mapper.OrderMapper;
import com.xingchen.backend.mapper.PaymentMapper;
import com.xingchen.backend.service.payment.AlipayService;
import com.xingchen.backend.util.Result;
import com.xingchen.backend.util.constant.Constants;
import com.xingchen.backend.util.constant.ErrorCode;
import com.xingchen.backend.util.id.SnowflakeIdWorker;
import com.xingchen.backend.util.idempotent.IdempotentUtil;
import com.xingchen.backend.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 支付创建控制器
 *
 * 仅支持支付宝沙箱支付
 * 包含完整的幂等性保护：
 * 1. 分布式锁防止重复创建支付
 * 2. 雪花算法生成唯一支付单号
 * 3. 数据库唯一索引最终防线
 *
 * @author 小跃
 * @date 2026-02-14
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Tag(name = "支付创建", description = "创建支付宝沙箱支付")
public class PaymentCreateController {

    private final AlipayService alipayService;
    private final OrderMapper orderMapper;
    private final PaymentMapper paymentMapper;
    private final IdempotentUtil idempotentUtil;

    /**
     * 创建支付宝沙箱支付
     */
    @SaCheckLogin
    @PostMapping("/alipay/create")
    @Operation(summary = "创建支付宝支付", description = "创建支付宝沙箱支付订单，返回支付表单HTML")
    public Result<String> createAlipayPayment(
            @Parameter(description = "订单ID") @RequestParam Long orderId) {

        Long userId = StpUtil.getLoginIdAsLong();

        // 查询订单
        Order order = orderMapper.selectOneById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXIST, "订单不存在");
        }

        // 验证订单归属
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.USER_NO_PERMISSION, "无权操作此订单");
        }

        // 验证订单状态
        if (order.getStatus() != Constants.OrderStatus.WAIT_PAY) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单状态不正确");
        }

        // 创建支付记录（带幂等性保护）
        Payment payment = createPaymentRecord(order);

        // 创建支付宝支付
        String formHtml = alipayService.createPcPayment(order, payment.getPaymentNo());

        log.info("创建支付宝支付成功, orderId={}, paymentNo={}", orderId, payment.getPaymentNo());

        return Result.success("支付宝支付创建成功", formHtml);
    }

    /**
     * 创建支付宝扫码支付（二维码支付）
     * 用户使用手机支付宝APP扫码完成支付
     */
    @SaCheckLogin
    @PostMapping("/alipay/qrcode")
    @Operation(summary = "创建支付宝扫码支付", description = "创建支付宝二维码，用户使用手机支付宝APP扫码支付")
    public Result<String> createAlipayQrCodePayment(
            @Parameter(description = "订单ID") @RequestParam Long orderId) {

        Long userId = StpUtil.getLoginIdAsLong();

        // 查询订单
        Order order = orderMapper.selectOneById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXIST, "订单不存在");
        }

        // 验证订单归属
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.USER_NO_PERMISSION, "无权操作此订单");
        }

        // 验证订单状态
        if (order.getStatus() != Constants.OrderStatus.WAIT_PAY) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单状态不正确");
        }

        // 创建支付记录（带幂等性保护）
        Payment payment = createPaymentRecord(order);

        // 创建支付宝扫码支付，获取二维码链接
        String qrCodeUrl = alipayService.createQrCodePayment(order, payment.getPaymentNo());

        log.info("创建支付宝扫码支付成功, orderId={}, paymentNo={}", orderId, payment.getPaymentNo());

        return Result.success("支付宝扫码支付创建成功", qrCodeUrl);
    }

    /**
     * 创建支付记录
     * 
     * 幂等性设计：
     * 1. 使用分布式锁防止并发重复创建
     * 2. 检查是否已存在未支付的支付记录
     * 3. 使用雪花算法生成唯一支付单号
     * 4. 数据库唯一索引作为最终防线
     */
    private Payment createPaymentRecord(Order order) {
        // 使用分布式锁防止并发重复创建
        String lockKey = "payment:create:" + order.getId();
        IdempotentUtil.IdempotentLock lock = idempotentUtil.tryLock(lockKey, 10);
        
        if (lock == null) {
            log.warn("支付创建正在处理中，orderId: {}", order.getId());
            throw new BusinessException(ErrorCode.PAYMENT_PROCESSING, "支付正在处理中，请稍后再试");
        }

        try {
            // 检查是否已存在未支付的支付记录
            Payment existingPayment = paymentMapper.selectByOrderId(order.getId());
            if (existingPayment != null) {
                if (existingPayment.getStatus() == Constants.PaymentStatus.UNPAID) {
                    log.info("存在未支付的支付记录，直接返回, orderId: {}, paymentNo: {}", 
                            order.getId(), existingPayment.getPaymentNo());
                    return existingPayment;
                } else if (existingPayment.getStatus() == Constants.PaymentStatus.PAID) {
                    log.warn("订单已支付，不能重复创建支付, orderId: {}", order.getId());
                    throw new BusinessException(ErrorCode.ORDER_ALREADY_PAID, "订单已支付");
                }
            }

            // 创建新的支付记录
            Payment payment = new Payment();
            payment.setOrderNo(order.getOrderNo());
            payment.setOrderId(order.getId());
            payment.setPaymentNo(generatePaymentNo());
            payment.setUserId(order.getUserId());
            payment.setAmount(order.getActualAmount());
            payment.setChannel(Constants.PaymentChannel.ALIPAY); // 支付宝
            payment.setStatus(Constants.PaymentStatus.UNPAID);
            payment.setVersion(0); // 初始版本号
            payment.setCreateTime(LocalDateTime.now());
            payment.setUpdateTime(LocalDateTime.now());

            paymentMapper.insert(payment);
            
            log.info("创建支付记录成功, orderId: {}, paymentNo: {}", order.getId(), payment.getPaymentNo());

            return payment;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 生成支付单号
     * 
     * 使用雪花算法生成全局唯一ID，格式：PAY + 雪花算法ID
     * 优点：
     * 1. 全局唯一，不会重复
     * 2. 趋势递增，对数据库友好
     * 3. 包含时间戳信息，可反解生成时间
     */
    private String generatePaymentNo() {
        return SnowflakeIdWorker.getInstance().nextId("PAY");
    }
}
