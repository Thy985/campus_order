package com.xingchen.backend.util.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLockUtil {
    
    private final StringRedisTemplate redisTemplate;
    
    /**
     * 锁前缀
     */
    private static final String LOCK_PREFIX = "lock:";
    
    /**
     * 默认锁过期时间（秒）
     */
    private static final long DEFAULT_EXPIRE_TIME = 30;
    
    /**
     * 释放锁的 Lua 脚本（原子操作：校验持有者 + 删除）
     * 只有当锁的值等于传入的 requestId 时才删除，避免误删其他线程的锁
     */
    private static final String UNLOCK_LUA_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";
    
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;
    
    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setScriptText(UNLOCK_LUA_SCRIPT);
        UNLOCK_SCRIPT.setResultType(Long.class);
    }
    
    /**
     * 获取锁
     *
     * @param lockKey 锁的key
     * @param requestId 请求标识（用于释放锁时校验）
     * @param expireTime 过期时间（秒）
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, String requestId, long expireTime) {
        String key = LOCK_PREFIX + lockKey;
        try {
            Boolean success = redisTemplate.opsForValue()
                    .setIfAbsent(key, requestId, expireTime, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(success)) {
                log.debug("获取锁成功: key={}, requestId={}", key, requestId);
                return true;
            }
            log.debug("获取锁失败: key={} 已被占用", key);
            return false;
        } catch (Exception e) {
            log.error("获取锁异常: key={}", key, e);
            return false;
        }
    }
    
    /**
     * 获取锁（使用默认过期时间）
     *
     * @param lockKey 锁的key
     * @param requestId 请求标识
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, String requestId) {
        return tryLock(lockKey, requestId, DEFAULT_EXPIRE_TIME);
    }
    
    /**
     * 释放锁（使用 Lua 脚本保证原子性）
     * 
     * 通过 Lua 脚本在 Redis 服务端原子执行"校验持有者 + 删除"操作，
     * 避免 GET 和 DELETE 之间的竞态条件导致误删其他线程的锁。
     *
     * @param lockKey 锁的key
     * @param requestId 请求标识
     * @return 是否释放成功
     */
    public boolean unlock(String lockKey, String requestId) {
        String key = LOCK_PREFIX + lockKey;
        try {
            Long result = redisTemplate.execute(
                    UNLOCK_SCRIPT,
                    Collections.singletonList(key),
                    requestId
            );
            boolean success = Long.valueOf(1L).equals(result);
            if (success) {
                log.debug("释放锁成功: key={}, requestId={}", key, requestId);
            } else {
                log.warn("释放锁失败: key={}, requestId={} (锁不存在或已被其他请求持有)", key, requestId);
            }
            return success;
        } catch (Exception e) {
            log.error("释放锁异常: key={}", key, e);
            return false;
        }
    }
}
