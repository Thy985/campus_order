package com.xingchen.backend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流配置
 */
@Configuration
public class RateLimitConfig {
    
    private final Map<String, Bucket> userBuckets = new ConcurrentHashMap<>();
    
    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();
    
    private final Map<String, Bucket> apiBuckets = new ConcurrentHashMap<>();
    
    /**
     * 创建订单限流 - 每用户每分钟10次
     */
    public Bucket createOrderBucket(Long userId) {
        String key = "order:create:" + userId;
        return userBuckets.computeIfAbsent(key, k -> {
            Bandwidth limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)));
            return Bucket.builder().addLimit(limit).build();
        });
    }
    
    /**
     * 登录限流 - 每IP每分钟20次（测试环境放宽限制）
     */
    public Bucket loginBucket(String ip) {
        String key = "login:" + ip;
        return ipBuckets.computeIfAbsent(key, k -> {
            Bandwidth limit = Bandwidth.classic(20, Refill.intervally(20, Duration.ofMinutes(1)));
            return Bucket.builder().addLimit(limit).build();
        });
    }
    
    /**
     * 通用API限流 - 每用户每秒50次
     */
    public Bucket apiBucket(Long userId) {
        String key = "api:" + userId;
        return userBuckets.computeIfAbsent(key, k -> {
            Bandwidth limit = Bandwidth.classic(50, Refill.intervally(50, Duration.ofSeconds(1)));
            return Bucket.builder().addLimit(limit).build();
        });
    }
    
    /**
     * 严格限流 - 敏感操作，每用户每分钟3次
     */
    public Bucket strictBucket(Long userId, String action) {
        String key = "strict:" + action + ":" + userId;
        return userBuckets.computeIfAbsent(key, k -> {
            Bandwidth limit = Bandwidth.classic(3, Refill.intervally(3, Duration.ofMinutes(1)));
            return Bucket.builder().addLimit(limit).build();
        });
    }
    
    /**
     * 清理过期的限流桶
     */
    public void cleanupBuckets() {
    }
}
