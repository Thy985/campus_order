package com.xingchen.backend.controller.order;

import com.xingchen.backend.service.payment.AlipayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝回调控制器
 * 
 * 包含完整的幂等性保护和签名验证
 *
 * @author xingchen
 * @date 2026-02-15
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/alipay")
@RequiredArgsConstructor
public class AlipayCallbackController {

    private final AlipayService alipayService;

    /**
     * 支付宝异步通知
     * 
     * 重要说明：
     * 1. 必须返回 "success" 或 "fail" 给支付宝
     * 2. 支付宝会重复通知直到收到 "success" 或超过最大重试次数
     * 3. 业务层已实现幂等性保护，可安全处理重复通知
     */
    @RequestMapping("/notify")
    @Transactional(rollbackFor = Exception.class)
    public String alipayNotify(HttpServletRequest request) {
        log.info("收到支付宝异步通知");

        try {
            // 获取支付宝通知参数
            Map<String, String> params = extractParams(request);
            log.info("支付宝通知参数: {}", params);

            // 参数校验
            if (params.isEmpty()) {
                log.error("支付宝通知参数为空");
                return "fail";
            }

            // 验证签名
            boolean signVerified = alipayService.verifyNotify(params);
            if (!signVerified) {
                log.error("支付宝签名验证失败");
                return "fail";
            }

            // 获取关键参数
            String outTradeNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");
            String tradeStatus = params.get("trade_status");
            String totalAmount = params.get("total_amount");

            // 参数完整性校验
            if (outTradeNo == null || outTradeNo.trim().isEmpty()) {
                log.error("支付宝通知缺少商户订单号");
                return "fail";
            }

            if (tradeStatus == null || tradeStatus.trim().isEmpty()) {
                log.error("支付宝通知缺少交易状态");
                return "fail";
            }

            log.info("支付宝通知 - 商户订单号: {}, 支付宝交易号: {}, 交易状态: {}, 金额: {}",
                    outTradeNo, tradeNo, tradeStatus, totalAmount);

            // 处理支付回调
            alipayService.handleNotify(
                    outTradeNo,
                    tradeNo,
                    tradeStatus,
                    new BigDecimal(totalAmount != null ? totalAmount : "0")
            );

            // 必须返回 success，否则支付宝会继续发送通知
            return "success";
        } catch (Exception e) {
            log.error("处理支付宝回调异常", e);
            // 返回 fail 让支付宝重试
            return "fail";
        }
    }

    /**
     * 支付宝同步通知（页面跳转）
     * 
     * 用户支付完成后跳转回商户页面
     * 注意：同步通知不可靠，仅用于页面展示，业务逻辑以异步通知为准
     */
    @RequestMapping("/return")
    public String alipayReturn(HttpServletRequest request) {
        log.info("收到支付宝同步通知");

        try {
            // 获取参数
            Map<String, String> params = extractParams(request);

            // 验证签名
            boolean signVerified = alipayService.verifyNotify(params);

            if (signVerified) {
                String outTradeNo = params.get("out_trade_no");
                String tradeNo = params.get("trade_no");
                log.info("支付宝支付成功 - 商户订单号: {}, 支付宝交易号: {}", outTradeNo, tradeNo);
                // 重定向到前端支付成功页面
                return "redirect:/#/payment/success?orderNo=" + outTradeNo;
            } else {
                log.error("支付宝同步通知签名验证失败");
                return "redirect:/#/payment/fail";
            }
        } catch (Exception e) {
            log.error("处理支付宝同步通知异常", e);
            return "redirect:/#/payment/fail";
        }
    }

    /**
     * 提取请求参数
     */
    private Map<String, String> extractParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            StringBuilder valueStr = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                valueStr.append(values[i]);
                if (i < values.length - 1) {
                    valueStr.append(",");
                }
            }
            params.put(name, valueStr.toString());
        }
        
        return params;
    }
}
