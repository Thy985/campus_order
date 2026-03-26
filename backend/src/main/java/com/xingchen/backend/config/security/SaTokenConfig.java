package com.xingchen.backend.config.security;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.filter.SaServletFilter;
import com.xingchen.backend.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sa-Token 配置类
 * 配置认证过滤器、路由规则等
 */
@Slf4j
@Configuration
public class SaTokenConfig {

    /**
     * 注册 Sa-Token 全局过滤器
     * 用于处理跨域、认证异常等
     */
    @Bean
    public SaServletFilter getSaServletFilter() {
        return new SaServletFilter()
                // 指定拦截路由
                .addInclude("/**")
                // 指定放行路由 - 包括支付宝/微信支付回调（不需要登录认证）
                .addExclude("/favicon.ico", "/swagger-ui/**", "/v3/api-docs/**", "/actuator/**",
                        "/api/payment/alipay/notify", "/api/payment/alipay/return", "/api/payment/wechat/notify")
                // 认证函数: 每次请求执行
                .setAuth(obj -> {
                    // 这里不做具体认证，由拦截器和注解控制
                })
                // 异常处理函数：每次认证函数发生异常时执行
                .setError(e -> {
                    log.error("Sa-Token 认证异常: {}", e.getMessage());
                    return Result.error(401, "请先登录");
                })
                // 前置函数：在每次认证函数之前执行
                .setBeforeAuth(obj -> {
                    // 设置跨域响应头
                    SaHolder.getResponse()
                            .setHeader("Access-Control-Allow-Origin", "*")
                            .setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                            .setHeader("Access-Control-Allow-Headers", "*")
                            .setHeader("Access-Control-Max-Age", "3600");
                });
    }
}
