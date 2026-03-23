package com.xingchen.backend.controller.order;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.xingchen.backend.entity.Order;
import com.xingchen.backend.service.OrderService;
import com.xingchen.backend.service.PaymentService;
import com.xingchen.backend.dto.request.payment.PaymentRequest;
import com.xingchen.backend.dto.response.payment.PaymentResponse;
import com.xingchen.backend.util.Result;
import com.xingchen.backend.util.constant.Constants;
import com.xingchen.backend.util.constant.ErrorCode;
import com.xingchen.backend.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 模拟支付控制器
 * 用于开发和测试环境
 *
 * @author 小跃
 * @date 2026-02-15
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/simulated")
@Tag(name = "模拟支付", description = "模拟支付接口（仅用于测试）")
@RequiredArgsConstructor
public class SimulatedPaymentController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    /**
     * 模拟支付 - 立即支付成功
     */
    @SaCheckLogin
    @PostMapping("/pay-success/{orderNo}")
    @Operation(summary = "模拟支付成功", description = "模拟支付成功场景，订单直接进入待接单状态")
    public Result<Void> simulatedPaySuccess(
            @Parameter(description = "订单号") @PathVariable String orderNo) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 查询订单
        Order order = orderService.getOrderByOrderNo(orderNo);

        // 验证订单归属
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.USER_NO_PERMISSION, "无权操作此订单");
        }

        // 防重检查：验证订单状态，确保只有待支付的订单才能支付
        if (order.getStatus() != Constants.OrderStatus.WAIT_PAY) {
            if (order.getPayStatus() == Constants.PayStatus.PAID) {
                throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单已支付，请勿重复操作");
            }
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单状态错误");
        }

        // 模拟支付成功
        orderService.paySuccess(order.getId());

        log.info("模拟支付成功: orderNo={}, userId={}", orderNo, userId);
        return Result.success("支付成功", null);
    }

    /**
     * 模拟支付 - 创建支付单并模拟支付流程
     */
    @SaCheckLogin
    @PostMapping("/create-and-pay/{orderNo}")
    @Operation(summary = "创建支付并立即成功", description = "创建支付单并立即模拟支付成功")
    public Result<PaymentResponse> createAndPay(
            @Parameter(description = "订单号") @PathVariable String orderNo) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 查询订单
        Order order = orderService.getOrderByOrderNo(orderNo);

        // 验证订单归属
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.USER_NO_PERMISSION, "无权操作此订单");
        }

        // 防重检查：验证订单状态，确保只有待支付的订单才能支付
        if (order.getStatus() != Constants.OrderStatus.WAIT_PAY) {
            if (order.getPayStatus() == Constants.PayStatus.PAID) {
                throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单已支付，请勿重复操作");
            }
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单状态错误");
        }

        // 创建支付单
        PaymentRequest request = new PaymentRequest();
        request.setOrderNo(orderNo);
        request.setAmount(order.getActualAmount());
        request.setPaymentMethod(Constants.PaymentMethod.ALIPAY);
        request.setDescription("模拟支付 - 订单" + orderNo);

        PaymentResponse response = paymentService.generatePaymentUrl(request);

        // 立即模拟支付成功
        orderService.paySuccess(order.getId());

        log.info("模拟创建支付并立即成功: orderNo={}, userId={}", orderNo, userId);
        return Result.success("支付成功", response);
    }

    /**
     * 模拟支付 - 支付失败
     */
    @SaCheckLogin
    @PostMapping("/pay-fail/{orderNo}")
    @Operation(summary = "模拟支付失败", description = "模拟支付失败场景")
    public Result<Void> simulatedPayFail(
            @Parameter(description = "订单号") @PathVariable String orderNo,
            @Parameter(description = "失败原因") @RequestParam(defaultValue = "余额不足") String reason) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 查询订单
        Order order = orderService.getOrderByOrderNo(orderNo);

        // 验证订单归属
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.USER_NO_PERMISSION, "无权操作此订单");
        }

        // 防重检查：验证订单状态
        if (order.getStatus() != Constants.OrderStatus.WAIT_PAY) {
            if (order.getPayStatus() == Constants.PayStatus.PAID) {
                throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单已支付，请勿重复操作");
            }
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "订单状态错误");
        }

        // 模拟支付失败（实际只是记录日志，订单保持待支付状态）
        log.info("模拟支付失败: orderNo={}, userId={}, reason={}", orderNo, userId, reason);

        return Result.error(ErrorCode.PAYMENT_FAILED, "支付失败: " + reason);
    }

    /**
     * 查询支付状态
     */
    @SaCheckLogin
    @GetMapping("/status/{orderNo}")
    @Operation(summary = "查询支付状态", description = "查询订单的支付状态")
    public Result<Integer> queryPayStatus(
            @Parameter(description = "订单号") @PathVariable String orderNo) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 查询订单
        Order order = orderService.getOrderByOrderNo(orderNo);

        // 验证订单归属
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.USER_NO_PERMISSION, "无权查询此订单");
        }

        return Result.success("查询成功", order.getPayStatus());
    }
}
