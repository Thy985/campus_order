package com.xingchen.backend.controller.order;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.xingchen.backend.entity.Payment;
import com.xingchen.backend.service.PaymentService;
import com.xingchen.backend.dto.request.payment.PaymentRequest;
import com.xingchen.backend.dto.response.payment.PaymentResponse;
import com.xingchen.backend.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 支付控制器 - 仅支持支付宝沙箱支付
 *
 * @author xingchen
 * @date 2026-02-13
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Tag(name = "支付管理", description = "支付宝沙箱支付相关接口")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 创建支付宝支付
     */
    @SaCheckLogin
    @PostMapping("/create")
    @Operation(summary = "创建支付宝支付", description = "创建支付宝沙箱支付订单，返回支付表单HTML")
    public Result<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        // 固定使用支付宝支付
        request.setPaymentMethod(2);
        PaymentResponse response = paymentService.generatePaymentUrl(request);
        return Result.success("支付宝支付单创建成功", response);
    }

    /**
     * 查询支付状态
     */
    @SaCheckLogin
    @GetMapping("/status/{orderNo}")
    @Operation(summary = "查询支付状态", description = "根据订单号查询支付状态")
    public Result<Integer> queryPaymentStatus(
            @Parameter(description = "订单号") @PathVariable String orderNo) {
        Integer status = paymentService.queryPaymentStatus(orderNo);
        return Result.success("查询成功", status);
    }

    /**
     * 获取支付记录
     */
    @SaCheckLogin
    @GetMapping("/record/{orderNo}")
    @Operation(summary = "获取支付记录", description = "根据订单号获取支付记录")
    public Result<Payment> getPaymentRecord(
            @Parameter(description = "订单号") @PathVariable String orderNo) {
        Payment payment = paymentService.getPaymentByOrderNo(orderNo);
        return Result.success(payment);
    }
}
