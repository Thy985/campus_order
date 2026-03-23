package com.xingchen.backend.util.idempotent;

import com.xingchen.backend.util.lock.RedisLockUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 幂等性工具类
 * 
 * 提供多种幂等性保护机制：
 * 1. Redis分布式锁 - 防止并发重复处理
 * 2. Redis令牌桶 - 防止重复请求
 * 3. 数据库唯一索引 - 最终防线
 *
 * @author xingchen
 * @date 2026-03-22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotentUtil {

    private final RedisLockUtil redisLockUtil;
    private final StringRedisTemplate redisTemplate;

    /**
     * 幂等性Key前缀
     */
    private static final String IDEMPOTENT_KEY_PREFIX = "idempotent:";
    
    /**
     * 幂等性令牌前缀
     */
    private static final String IDEMPOTENT_TOKEN_PREFIX = "idempotent:token:";

    /**
     * 默认锁过期时间（秒）
     */
    private static final long DEFAULT_LOCK_EXPIRE_TIME = 30;

    /**
     * 默认幂等性令牌过期时间（秒）
     */
    private static final long DEFAULT_TOKEN_EXPIRE_TIME = 300; // 5分钟

    /**
     * 获取分布式锁进行幂等性控制
     * 
     * 使用场景：支付回调、订单创建等需要防止重复处理的场景
     *
     * @param key 业务唯一标识，如订单号、支付单号
     * @return 锁信息，如果获取失败返回null
     */
    public IdempotentLock tryLock(String key) {
        String lockKey = IDEMPOTENT_KEY_PREFIX + "lock:" + key;
        String requestId = UUID.randomUUID().toString();
        
        boolean success = redisLockUtil.tryLock(lockKey, requestId, DEFAULT_LOCK_EXPIRE_TIME);
        if (!success) {
            log.warn("获取幂等性锁失败，可能正在处理中: {}", key);
            return null;
        }
        
        log.debug("获取幂等性锁成功: key={}, requestId={}", key, requestId);
        return new IdempotentLock(lockKey, requestId, redisLockUtil);
    }

    /**
     * 获取分布式锁进行幂等性控制（自定义过期时间）
     *
     * @param key 业务唯一标识
     * @param expireTime 过期时间（秒）
     * @return 锁信息，如果获取失败返回null
     */
    public IdempotentLock tryLock(String key, long expireTime) {
        String lockKey = IDEMPOTENT_KEY_PREFIX + "lock:" + key;
        String requestId = UUID.randomUUID().toString();
        
        boolean success = redisLockUtil.tryLock(lockKey, requestId, expireTime);
        if (!success) {
            log.warn("获取幂等性锁失败，可能正在处理中: {}", key);
            return null;
        }
        
        log.debug("获取幂等性锁成功: key={}, requestId={}", key, requestId);
        return new IdempotentLock(lockKey, requestId, redisLockUtil);
    }

    /**
     * 检查是否已处理（基于Redis SetNX）
     * 
     * 使用场景：需要严格保证只执行一次的场景
     *
     * @param key 业务唯一标识
     * @return true-未处理，可以执行；false-已处理或正在处理
     */
    public boolean checkAndMark(String key) {
        String redisKey = IDEMPOTENT_KEY_PREFIX + "processed:" + key;
        
        // 使用setIfAbsent原子操作
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "1", DEFAULT_TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);
        
        if (Boolean.TRUE.equals(success)) {
            log.debug("标记为已处理: {}", key);
            return true;
        }
        
        log.warn("重复请求，已处理或正在处理: {}", key);
        return false;
    }

    /**
     * 检查是否已处理（自定义过期时间）
     *
     * @param key 业务唯一标识
     * @param expireTime 过期时间（秒）
     * @return true-未处理，可以执行；false-已处理或正在处理
     */
    public boolean checkAndMark(String key, long expireTime) {
        String redisKey = IDEMPOTENT_KEY_PREFIX + "processed:" + key;
        
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "1", expireTime, TimeUnit.SECONDS);
        
        if (Boolean.TRUE.equals(success)) {
            log.debug("标记为已处理: {}", key);
            return true;
        }
        
        log.warn("重复请求，已处理或正在处理: {}", key);
        return false;
    }

    /**
     * 标记为已处理完成（用于延长过期时间）
     *
     * @param key 业务唯一标识
     * @param expireTime 过期时间（秒）
     */
    public void markCompleted(String key, long expireTime) {
        String redisKey = IDEMPOTENT_KEY_PREFIX + "processed:" + key;
        redisTemplate.opsForValue().set(redisKey, "1", expireTime, TimeUnit.SECONDS);
        log.debug("标记处理完成: {}", key);
    }

    /**
     * 清除处理标记
     *
     * @param key 业务唯一标识
     */
    public void clearMark(String key) {
        String redisKey = IDEMPOTENT_KEY_PREFIX + "processed:" + key;
        redisTemplate.delete(redisKey);
        log.debug("清除处理标记: {}", key);
    }

    /**
     * 生成幂等性令牌（用于前端防重）
     * 
     * 使用场景：表单提交前获取令牌，提交时携带
     *
     * @return 幂等性令牌
     */
    public String generateToken() {
        String token = UUID.randomUUID().toString().replace("-", "");
        String redisKey = IDEMPOTENT_TOKEN_PREFIX + token;
        
        redisTemplate.opsForValue().set(redisKey, "1", DEFAULT_TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);
        log.debug("生成幂等性令牌: {}", token);
        
        return token;
    }

    /**
     * 验证并消耗幂等性令牌
     *
     * @param token 幂等性令牌
     * @return true-验证通过；false-令牌无效或已使用
     */
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        String redisKey = IDEMPOTENT_TOKEN_PREFIX + token;
        Boolean deleted = redisTemplate.delete(redisKey);
        
        if (Boolean.TRUE.equals(deleted)) {
            log.debug("验证幂等性令牌成功: {}", token);
            return true;
        }
        
        log.warn("验证幂等性令牌失败，可能已使用或过期: {}", token);
        return false;
    }

    /**
     * 幂等性锁对象
     * 用于自动释放锁
     */
    public static class IdempotentLock implements AutoCloseable {
        private final String lockKey;
        private final String requestId;
        private final RedisLockUtil redisLockUtil;
        private boolean locked;

        public IdempotentLock(String lockKey, String requestId, RedisLockUtil redisLockUtil) {
            this.lockKey = lockKey;
            this.requestId = requestId;
            this.redisLockUtil = redisLockUtil;
            this.locked = true;
        }

        /**
         * 释放锁
         */
        public void unlock() {
            if (locked) {
                redisLockUtil.unlock(lockKey, requestId);
                locked = false;
                log.debug("释放幂等性锁: {}", lockKey);
            }
        }

        @Override
        public void close() {
            unlock();
        }

        public boolean isLocked() {
            return locked;
        }
    }
}
