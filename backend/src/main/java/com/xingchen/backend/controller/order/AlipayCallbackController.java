package com.xingchen.backend.controller.order;

import com.xingchen.backend.service.payment.AlipayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
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
        log.info("========================================");
        log.info("【支付宝回调】收到支付宝异步通知");
        log.info("回调URL: {}", request.getRequestURL());
        log.info("请求方法: {}", request.getMethod());

        try {
            // 获取支付宝通知参数
            Map<String, String> params = extractParams(request);
            log.info("【支付宝回调】通知参数数量: {}", params.size());
            log.info("【支付宝回调】通知参数: {}", params);

            // 参数校验
            if (params.isEmpty()) {
                log.error("【支付宝回调】通知参数为空");
                return "fail";
            }

            // 打印关键参数
            String outTradeNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");
            String tradeStatus = params.get("trade_status");
            String totalAmount = params.get("total_amount");
            String sign = params.get("sign");
            
            log.info("【支付宝回调】商户订单号: {}", outTradeNo);
            log.info("【支付宝回调】支付宝交易号: {}", tradeNo);
            log.info("【支付宝回调】交易状态: {}", tradeStatus);
            log.info("【支付宝回调】支付金额: {}", totalAmount);
            log.info("【支付宝回调】签名前50字符: {}", sign != null ? sign.substring(0, Math.min(50, sign.length())) : "null");

            // 参数完整性校验
            if (outTradeNo == null || outTradeNo.trim().isEmpty()) {
                log.error("【支付宝回调】缺少商户订单号");
                return "fail";
            }

            // 如果没有交易状态，默认为 TRADE_SUCCESS（兼容同步通知）
            if (tradeStatus == null || tradeStatus.trim().isEmpty()) {
                log.warn("【支付宝回调】缺少交易状态，默认为 TRADE_SUCCESS");
                tradeStatus = "TRADE_SUCCESS";
            }

            // 验证签名
            log.info("【支付宝回调】开始验证签名...");
            boolean signVerified = alipayService.verifyNotify(params);
            if (!signVerified) {
                log.error("【支付宝回调】签名验证失败，请检查：");
                log.error("  1. 支付宝公钥配置是否正确");
                log.error("  2. 应用ID是否匹配");
                log.error("  3. 是否使用沙箱环境");
                return "fail";
            }
            log.info("【支付宝回调】签名验证成功");

            // 处理支付回调
            log.info("【支付宝回调】开始处理支付回调...");
            alipayService.handleNotify(
                    outTradeNo,
                    tradeNo,
                    tradeStatus,
                    new BigDecimal(totalAmount != null ? totalAmount : "0")
            );

            log.info("【支付宝回调】处理完成，返回 success");
            log.info("========================================");
            // 必须返回 success，否则支付宝会继续发送通知
            return "success";
        } catch (Exception e) {
            log.error("【支付宝回调】处理异常", e);
            log.info("========================================");
            // 返回 fail 让支付宝重试
            return "fail";
        }
    }

    /**
     * 支付宝同步通知（页面跳转）
     * 
     * 用户支付完成后跳转回商户页面
     * 注意：同步通知不可靠，但这里也尝试更新订单状态，作为异步通知的备份
     */
    @RequestMapping("/return")
    public void alipayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("========================================");
        log.info("【支付宝同步通知】收到同步通知");
        log.info("【支付宝同步通知】请求URL: {}", request.getRequestURL());
        log.info("【支付宝同步通知】请求QueryString: {}", request.getQueryString());

        String redirectUrl;
        String outTradeNo = null;
        try {
            // 获取参数
            Map<String, String> params = extractParams(request);
            log.info("【支付宝同步通知】参数数量: {}", params.size());
            log.info("【支付宝同步通知】参数: {}", params);

            // 打印关键参数
            outTradeNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");
            String totalAmount = params.get("total_amount");
            String sign = params.get("sign");
            String tradeStatus = params.get("trade_status");
            
            log.info("【支付宝同步通知】商户订单号: {}", outTradeNo);
            log.info("【支付宝同步通知】支付宝交易号: {}", tradeNo);
            log.info("【支付宝同步通知】交易状态: {}", tradeStatus);
            log.info("【支付宝同步通知】支付金额: {}", totalAmount);
            log.info("【支付宝同步通知】签名前50字符: {}", sign != null ? sign.substring(0, Math.min(50, sign.length())) : "null");

            // 验证签名
            log.info("【支付宝同步通知】开始验证签名...");
            boolean signVerified = alipayService.verifyNotify(params);
            log.info("【支付宝同步通知】签名验证结果: {}", signVerified);

            if (signVerified) {
                log.info("【支付宝同步通知】签名验证成功，尝试更新订单状态");
                
                // 同步通知也尝试更新订单状态（作为异步通知的备份）
                try {
                    if (outTradeNo != null && !outTradeNo.trim().isEmpty()) {
                        alipayService.handleNotify(
                                outTradeNo,
                                tradeNo,
                                tradeStatus != null ? tradeStatus : "TRADE_SUCCESS",
                                new BigDecimal(totalAmount != null ? totalAmount : "0")
                        );
                        log.info("【支付宝同步通知】订单状态更新成功");
                    }
                } catch (Exception ex) {
                    // 同步通知更新失败不影响页面跳转，异步通知会再次尝试
                    log.warn("【支付宝同步通知】订单状态更新失败（异步通知会重试）: {}", ex.getMessage());
                }
                
                // 重定向到前端支付成功页面
                redirectUrl = "http://localhost:5173/#/payment/success?orderNo=" + outTradeNo;
            } else {
                log.error("【支付宝同步通知】签名验证失败，跳转到失败页面");
                log.error("  请检查支付宝公钥配置是否正确");
                redirectUrl = "http://localhost:5173/#/payment/fail";
            }
        } catch (Exception e) {
            log.error("【支付宝同步通知】处理异常", e);
            redirectUrl = "http://localhost:5173/#/payment/fail";
        }
        
        log.info("【支付宝同步通知】重定向到: {}", redirectUrl);
        log.info("========================================");
        response.sendRedirect(redirectUrl);
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
