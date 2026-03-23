package com.xingchen.backend.config.security;

import cn.dev33.satoken.stp.StpInterface;
import com.xingchen.backend.entity.User;
import com.xingchen.backend.enums.UserType;
import com.xingchen.backend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ROLE_CACHE_PREFIX = "satoken:roles:";
    private static final String PERMISSION_CACHE_PREFIX = "satoken:permissions:";
    private static final long CACHE_EXPIRE_MINUTES = 30;
    private static final long NULL_CACHE_EXPIRE_MINUTES = 5;

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = convertToUserId(loginId);
        if (userId == null) {
            return Collections.emptyList();
        }

        String cacheKey = ROLE_CACHE_PREFIX + userId + ":" + loginType;
        @SuppressWarnings("unchecked")
        List<String> cachedRoles = (List<String>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedRoles != null) {
            return cachedRoles;
        }

        User user = userMapper.selectOneById(userId);
        if (user == null) {
            redisTemplate.opsForValue().set(cacheKey, Collections.emptyList(), NULL_CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            log.debug("Cached empty roles for non-existent user {}", userId);
            return Collections.emptyList();
        }

        UserType userType = UserType.fromCode(user.getUserType());
        List<String> roles = userType.getRolesByLoginType(loginType);

        redisTemplate.opsForValue().set(cacheKey, roles, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        log.debug("Cached roles for user {} with loginType {}: {}", userId, loginType, roles);

        return roles;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = convertToUserId(loginId);
        if (userId == null) {
            return Collections.emptyList();
        }

        String cacheKey = PERMISSION_CACHE_PREFIX + userId + ":" + loginType;
        @SuppressWarnings("unchecked")
        List<String> cachedPermissions = (List<String>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedPermissions != null) {
            return cachedPermissions;
        }

        User user = userMapper.selectOneById(userId);
        if (user == null) {
            redisTemplate.opsForValue().set(cacheKey, Collections.emptyList(), NULL_CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            log.debug("Cached empty permissions for non-existent user {}", userId);
            return Collections.emptyList();
        }

        UserType userType = UserType.fromCode(user.getUserType());
        List<String> permissions = userType.getPermissionsByLoginType(loginType);

        redisTemplate.opsForValue().set(cacheKey, permissions, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        log.debug("Cached permissions for user {} with loginType {}: {}", userId, loginType, permissions);

        return permissions;
    }

    public void invalidateUserCache(Long userId) {
        if (userId == null) {
            return;
        }

        Set<String> roleKeys = redisTemplate.keys(ROLE_CACHE_PREFIX + userId + ":*");
        Set<String> permKeys = redisTemplate.keys(PERMISSION_CACHE_PREFIX + userId + ":*");

        if (roleKeys != null && !roleKeys.isEmpty()) {
            redisTemplate.delete(roleKeys);
            log.info("Invalidated {} role cache entries for user {}", roleKeys.size(), userId);
        }

        if (permKeys != null && !permKeys.isEmpty()) {
            redisTemplate.delete(permKeys);
            log.info("Invalidated {} permission cache entries for user {}", permKeys.size(), userId);
        }
    }

    private Long convertToUserId(Object loginId) {
        if (loginId == null) {
            log.warn("loginId is null");
            return null;
        }

        if (loginId instanceof Long) {
            return (Long) loginId;
        } else if (loginId instanceof Integer) {
            return ((Integer) loginId).longValue();
        } else if (loginId instanceof String) {
            String loginIdStr = (String) loginId;
            if (loginIdStr.isEmpty()) {
                log.warn("loginId is empty string");
                return null;
            }
            try {
                return Long.parseLong(loginIdStr);
            } catch (NumberFormatException e) {
                log.error("Failed to parse loginId as Long: {}", loginIdStr, e);
                return null;
            }
        } else {
            log.error("Unsupported loginId type: {}", loginId.getClass());
            return null;
        }
    }
}