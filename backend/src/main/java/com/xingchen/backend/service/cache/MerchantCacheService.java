package com.xingchen.backend.service.cache;

import com.xingchen.backend.dto.response.merchant.MerchantDetailResponse;
import com.xingchen.backend.dto.response.merchant.MerchantListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 商户缓存服务
 * 负责商户相关数据缓存 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String MERCHANT_LIST_KEY = "merchant:list:";
    private static final String MERCHANT_DETAIL_KEY = "merchant:detail:";
    private static final String MERCHANT_STATS_KEY = "merchant:stats:";
    private static final long CACHE_TTL = 30; // 30鍒嗛挓

    /**
     * 缂撳瓨鍟嗗鍒楄〃
     */
    public void cacheMerchantList(String key, MerchantListResponse response) {
        String cacheKey = MERCHANT_LIST_KEY + key;
        redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL, TimeUnit.MINUTES);
        log.debug("缂撳瓨鍟嗗鍒楄〃: {}", cacheKey);
    }

    /**
     * 鑾峰彇缂撳瓨鐨勫晢瀹跺垪琛?     */
    public MerchantListResponse getCachedMerchantList(String key) {
        String cacheKey = MERCHANT_LIST_KEY + key;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("鍛戒腑鍟嗗鍒楄〃缂撳瓨: {}", cacheKey);
            return (MerchantListResponse) cached;
        }
        return null;
    }

    /**
     * 缂撳瓨鍟嗗璇︽儏
     */
    public void cacheMerchantDetail(Long merchantId, MerchantDetailResponse response) {
        String cacheKey = MERCHANT_DETAIL_KEY + merchantId;
        redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL, TimeUnit.MINUTES);
        log.debug("缂撳瓨鍟嗗璇︽儏: {}", cacheKey);
    }

    /**
     * 鑾峰彇缂撳瓨鐨勫晢瀹惰鎯?     */
    public MerchantDetailResponse getCachedMerchantDetail(Long merchantId) {
        String cacheKey = MERCHANT_DETAIL_KEY + merchantId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("鍛戒腑鍟嗗璇︽儏缂撳瓨: {}", cacheKey);
            return (MerchantDetailResponse) cached;
        }
        return null;
    }

    /**
     * 娓呴櫎鍟嗗璇︽儏缂撳瓨
     */
    public void clearMerchantDetailCache(Long merchantId) {
        String cacheKey = MERCHANT_DETAIL_KEY + merchantId;
        redisTemplate.delete(cacheKey);
        log.debug("娓呴櫎鍟嗗璇︽儏缂撳瓨: {}", cacheKey);
    }

    /**
     * 缂撳瓨鍟嗗缁熻鏁版嵁
     */
    public void cacheMerchantStats(Long merchantId, Object stats) {
        String cacheKey = MERCHANT_STATS_KEY + merchantId;
        redisTemplate.opsForValue().set(cacheKey, stats, 5, TimeUnit.MINUTES); // 缁熻鏁版嵁缂撳瓨5鍒嗛挓
        log.debug("缂撳瓨鍟嗗缁熻: {}", cacheKey);
    }

    /**
     * 鑾峰彇缂撳瓨鐨勫晢瀹剁粺璁℃暟鎹?     */
    public Object getCachedMerchantStats(Long merchantId) {
        String cacheKey = MERCHANT_STATS_KEY + merchantId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("鍛戒腑鍟嗗缁熻缂撳瓨: {}", cacheKey);
            return cached;
        }
        return null;
    }

    /**
     * 娓呴櫎鍟嗗缁熻缂撳瓨
     */
    public void clearMerchantStatsCache(Long merchantId) {
        String cacheKey = MERCHANT_STATS_KEY + merchantId;
        redisTemplate.delete(cacheKey);
        log.debug("娓呴櫎鍟嗗缁熻缂撳瓨: {}", cacheKey);
    }

    /**
     * 娓呴櫎鎵€鏈夊晢瀹剁浉鍏崇紦瀛?     */
    public void clearAllMerchantCache(Long merchantId) {
        clearMerchantDetailCache(merchantId);
        clearMerchantStatsCache(merchantId);
        // 娓呴櫎鍒楄〃缂撳瓨锛堜娇鐢ㄩ€氶厤绗︼級
        redisTemplate.delete(redisTemplate.keys(MERCHANT_LIST_KEY + "*"));
        log.info("娓呴櫎鍟嗗鎵€鏈夌紦瀛? {}", merchantId);
    }
}
