package com.xingchen.backend.service.cache;

import com.xingchen.backend.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 商品缓存服务
 * 负责商家商品相关数据缓存 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PRODUCT_LIST_KEY = "product:list:";
    private static final String PRODUCT_DETAIL_KEY = "product:detail:";
    private static final String PRODUCT_CATEGORY_KEY = "product:category:";
    private static final long CACHE_TTL = 60; // 60鍒嗛挓

    /**
     * 缂撳瓨鍟嗗搧鍒楄〃
     */
    public void cacheProductList(Long merchantId, List<Product> products) {
        String cacheKey = PRODUCT_LIST_KEY + merchantId;
        redisTemplate.opsForValue().set(cacheKey, products, CACHE_TTL, TimeUnit.MINUTES);
        log.debug("缂撳瓨鍟嗗搧鍒楄〃: {}", cacheKey);
    }

    /**
     * 鑾峰彇缂撳瓨鐨勫晢鍝佸垪琛?     */
    @SuppressWarnings("unchecked")
    public List<Product> getCachedProductList(Long merchantId) {
        String cacheKey = PRODUCT_LIST_KEY + merchantId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("鍛戒腑鍟嗗搧鍒楄〃缂撳瓨: {}", cacheKey);
            return (List<Product>) cached;
        }
        return null;
    }

    /**
     * 缂撳瓨鍟嗗搧璇︽儏
     */
    public void cacheProductDetail(Long productId, Product product) {
        String cacheKey = PRODUCT_DETAIL_KEY + productId;
        redisTemplate.opsForValue().set(cacheKey, product, CACHE_TTL, TimeUnit.MINUTES);
        log.debug("缂撳瓨鍟嗗搧璇︽儏: {}", cacheKey);
    }

    /**
     * 鑾峰彇缂撳瓨鐨勫晢鍝佽鎯?     */
    public Product getCachedProductDetail(Long productId) {
        String cacheKey = PRODUCT_DETAIL_KEY + productId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("鍛戒腑鍟嗗搧璇︽儏缂撳瓨: {}", cacheKey);
            return (Product) cached;
        }
        return null;
    }

    /**
     * 娓呴櫎鍟嗗搧璇︽儏缂撳瓨
     */
    public void clearProductDetailCache(Long productId) {
        String cacheKey = PRODUCT_DETAIL_KEY + productId;
        redisTemplate.delete(cacheKey);
        log.debug("娓呴櫎鍟嗗搧璇︽儏缂撳瓨: {}", cacheKey);
    }

    /**
     * 缂撳瓨鍟嗗搧鍒嗙被
     */
    public void cacheProductCategories(Long merchantId, Object categories) {
        String cacheKey = PRODUCT_CATEGORY_KEY + merchantId;
        redisTemplate.opsForValue().set(cacheKey, categories, CACHE_TTL, TimeUnit.MINUTES);
        log.debug("缂撳瓨鍟嗗搧鍒嗙被: {}", cacheKey);
    }

    /**
     * 鑾峰彇缂撳瓨鐨勫晢鍝佸垎绫?     */
    public Object getCachedProductCategories(Long merchantId) {
        String cacheKey = PRODUCT_CATEGORY_KEY + merchantId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("鍛戒腑鍟嗗搧鍒嗙被缂撳瓨: {}", cacheKey);
            return cached;
        }
        return null;
    }

    /**
     * 娓呴櫎鍟嗗鐨勬墍鏈夊晢鍝佺紦瀛?     */
    public void clearMerchantProductsCache(Long merchantId) {
        redisTemplate.delete(PRODUCT_LIST_KEY + merchantId);
        redisTemplate.delete(PRODUCT_CATEGORY_KEY + merchantId);
        log.info("娓呴櫎鍟嗗鍟嗗搧缂撳瓨: {}", merchantId);
    }
}
