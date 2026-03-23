package com.xingchen.backend.service.cache;

import com.xingchen.backend.entity.Order;
import com.xingchen.backend.dto.response.order.OrderDetailDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 订单缓存服务
 * 负责订单相关数据缓存 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ORDER_DETAIL_KEY = "order:detail:";
    private static final String USER_ORDERS_KEY = "order:user:";
    private static final String MERCHANT_ORDERS_KEY = "order:merchant:";
    private static final long DETAIL_CACHE_TTL = 30; // 璁㈠崟璇︽儏缂撳瓨30鍒嗛挓
    private static final long LIST_CACHE_TTL = 10;   // 璁㈠崟鍒楄〃缂撳瓨10鍒嗛挓

    /**
     * 缂撳瓨璁㈠崟璇︽儏
     */
    public void cacheOrderDetail(Long orderId, OrderDetailDTO detail) {
        String cacheKey = ORDER_DETAIL_KEY + orderId;
        redisTemplate.opsForValue().set(cacheKey, detail, DETAIL_CACHE_TTL, TimeUnit.MINUTES);
        log.debug("缂撳瓨璁㈠崟璇︽儏: {}", cacheKey);
    }

    /**
     * 鑾峰彇缂撳瓨鐨勮鍗曡鎯?     */
    public OrderDetailDTO getCachedOrderDetail(Long orderId) {
        String cacheKey = ORDER_DETAIL_KEY + orderId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("鍛戒腑璁㈠崟璇︽儏缂撳瓨: {}", cacheKey);
            return (OrderDetailDTO) cached;
        }
        return null;
    }

    /**
     * 娓呴櫎璁㈠崟璇︽儏缂撳瓨
     */
    public void clearOrderDetailCache(Long orderId) {
        String cacheKey = ORDER_DETAIL_KEY + orderId;
        redisTemplate.delete(cacheKey);
        log.debug("娓呴櫎璁㈠崟璇︽儏缂撳瓨: {}", cacheKey);
    }

    /**
     * 缂撳瓨鐢ㄦ埛璁㈠崟鍒楄〃
     */
    public void cacheUserOrders(Long userId, List<Order> orders) {
        String cacheKey = USER_ORDERS_KEY + userId;
        redisTemplate.opsForValue().set(cacheKey, orders, LIST_CACHE_TTL, TimeUnit.MINUTES);
        log.debug("缂撳瓨鐢ㄦ埛璁㈠崟鍒楄〃: {}", cacheKey);
    }

    /**
     * 鑾峰彇缂撳瓨鐨勭敤鎴疯鍗曞垪琛?     */
    @SuppressWarnings("unchecked")
    public List<Order> getCachedUserOrders(Long userId) {
        String cacheKey = USER_ORDERS_KEY + userId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("鍛戒腑鐢ㄦ埛璁㈠崟鍒楄〃缂撳瓨: {}", cacheKey);
            return (List<Order>) cached;
        }
        return null;
    }

    /**
     * 娓呴櫎鐢ㄦ埛璁㈠崟鍒楄〃缂撳瓨
     */
    public void clearUserOrdersCache(Long userId) {
        String cacheKey = USER_ORDERS_KEY + userId;
        redisTemplate.delete(cacheKey);
        log.debug("娓呴櫎鐢ㄦ埛璁㈠崟鍒楄〃缂撳瓨: {}", cacheKey);
    }

    /**
     * 缂撳瓨鍟嗗璁㈠崟鍒楄〃
     */
    public void cacheMerchantOrders(Long merchantId, List<Order> orders) {
        String cacheKey = MERCHANT_ORDERS_KEY + merchantId;
        redisTemplate.opsForValue().set(cacheKey, orders, LIST_CACHE_TTL, TimeUnit.MINUTES);
        log.debug("缂撳瓨鍟嗗璁㈠崟鍒楄〃: {}", cacheKey);
    }

    /**
     * 鑾峰彇缂撳瓨鐨勫晢瀹惰鍗曞垪琛?     */
    @SuppressWarnings("unchecked")
    public List<Order> getCachedMerchantOrders(Long merchantId) {
        String cacheKey = MERCHANT_ORDERS_KEY + merchantId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("鍛戒腑鍟嗗璁㈠崟鍒楄〃缂撳瓨: {}", cacheKey);
            return (List<Order>) cached;
        }
        return null;
    }

    /**
     * 娓呴櫎鍟嗗璁㈠崟鍒楄〃缂撳瓨
     */
    public void clearMerchantOrdersCache(Long merchantId) {
        String cacheKey = MERCHANT_ORDERS_KEY + merchantId;
        redisTemplate.delete(cacheKey);
        log.debug("娓呴櫎鍟嗗璁㈠崟鍒楄〃缂撳瓨: {}", cacheKey);
    }

    /**
     * 璁㈠崟鐘舵€佸彉鏇存椂娓呴櫎鐩稿叧缂撳瓨
     */
    public void clearOrderRelatedCache(Long orderId, Long userId, Long merchantId) {
        clearOrderDetailCache(orderId);
        if (userId != null) {
            clearUserOrdersCache(userId);
        }
        if (merchantId != null) {
            clearMerchantOrdersCache(merchantId);
        }
        log.info("娓呴櫎璁㈠崟鐩稿叧缂撳瓨: orderId={}, userId={}, merchantId={}", orderId, userId, merchantId);
    }
}
