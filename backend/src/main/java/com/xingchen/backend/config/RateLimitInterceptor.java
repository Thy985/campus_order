package com.xingchen.backend.config;

import cn.dev33.satoken.stp.StpUtil;
import com.xingchen.backend.util.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

/**
 * API限流拦截器
 *
 * <p>基于令牌桶算法实现多维度限流控制</p>
 *
 * <p>限流策略：</p>
 * <ul>
 *   <li>登录/验证码接口 - 每IP每分钟20次（防暴力破解）</li>
 *   <li>创建订单接口 - 每用户每分钟10次（防刷单）</li>
 *   <li>通用API接口 - 每用户每秒50次（防爬虫）</li>
 * </ul>
 *
 * @author xingchen
 * @since 1.0.0
 * @see RateLimitConfig
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitConfig rateLimitConfig;
    private final ObjectMapper objectMapper;

    /**
     * 请求前置处理 - 执行限流检查
     *
     * <p>检查顺序：</p>
     * <ol>
     *   <li>登录/验证码接口限流（基于IP）</li>
     *   <li>创建订单接口限流（基于用户ID）</li>
     *   <li>通用API限流（基于用户ID）</li>
     * </ol>
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param handler  处理器
     * @return true-允许通过，false-拒绝请求
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String clientIp = getClientIp(request);

        // 1. 登录/验证码接口限流
        if (uri.contains("/api/auth/login") || uri.contains("/api/auth/verify-code")) {
            if (!rateLimitConfig.loginBucket(clientIp).tryConsume(1)) {
                log.warn("IP[{}] login/verify code request too frequent", clientIp);
                sendErrorResponse(response, 429, "Request too frequent, please try again later");
                return false;
            }
        }

        // 2. 创建订单接口限流
        if (uri.contains("/api/order") && "POST".equals(method)) {
            if (!StpUtil.isLogin()) {
                return true;
            }
            Long userId = StpUtil.getLoginIdAsLong();
            if (!rateLimitConfig.createOrderBucket(userId).tryConsume(1)) {
                log.warn("User[{}] creating order too frequently", userId);
                sendErrorResponse(response, 429, "Order too frequent, please try again later");
                return false;
            }
        }

        // 3. 通用API限流
        if (StpUtil.isLogin()) {
            Long userId = StpUtil.getLoginIdAsLong();
            if (!rateLimitConfig.apiBucket(userId).tryConsume(1)) {
                log.warn("User[{}] request too frequent", userId);
                sendErrorResponse(response, 429, "Request too frequent, please try again later");
                return false;
            }
        }

        return true;
    }

    /**
     * 发送限流错误响应
     *
     * @param response HTTP响应对象
     * @param code     HTTP状态码
     * @param message  错误信息
     * @throws Exception 写入响应时可能抛出的异常
     */
    private void sendErrorResponse(HttpServletResponse response, int code, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(code);

        Result<Void> result = Result.error(code, message);
        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(result));
        writer.flush();
    }

    /**
     * 获取客户端真实IP地址
     *
     * <p>支持反向代理环境，按优先级检查以下Header：</p>
     * <ol>
     *   <li>X-Forwarded-For</li>
     *   <li>Proxy-Client-IP</li>
     *   <li>WL-Proxy-Client-IP</li>
     *   <li>HTTP_CLIENT_IP</li>
     *   <li>HTTP_X_FORWARDED_FOR</li>
     *   <li>RemoteAddr（直接连接）</li>
     * </ol>
     *
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For可能包含多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
