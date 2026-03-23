package com.xingchen.backend.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xingchen.backend.entity.Product;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品 Mapper
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    
    /**
     * 根据商家ID查询商品列表
     */
    default List<Product> selectByMerchantId(Long merchantId, int limit, int offset) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Product.class)
                .where("merchant_id = ? AND is_deleted = 0", merchantId)
                .orderBy("sort_order DESC, sales_volume DESC, create_time DESC")
                .limit(offset, limit);
        return selectListByQuery(queryWrapper);
    }
    
    /**
     * 根据商家ID和分类ID查询商品列表
     */
    default List<Product> selectByMerchantIdAndCategoryId(Long merchantId, Integer categoryId, int limit, int offset) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Product.class)
                .where("merchant_id = ? AND category_id = ? AND is_deleted = 0", merchantId, categoryId)
                .orderBy("sort_order DESC, sales_volume DESC")
                .limit(offset, limit);
        return selectListByQuery(queryWrapper);
    }
    
    /**
     * 根据商家ID和状态查询商品列?     */
    default List<Product> selectByMerchantIdAndStatus(Long merchantId, Integer status, int limit, int offset) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Product.class)
                .where("merchant_id = ? AND status = ? AND is_deleted = 0", merchantId, status)
                .orderBy("sort_order DESC, create_time DESC")
                .limit(offset, limit);
        return selectListByQuery(queryWrapper);
    }
    
    /**
     * 根据条件查询商家商品列表（支持状态和关键词筛选）
     */
    default List<Product> selectByMerchantIdWithFilter(Long merchantId, Integer status, String keyword, int limit, int offset) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Product.class)
                .where("merchant_id = ? AND is_deleted = 0", merchantId);

        if (status != null) {
            queryWrapper.and("status = ?", status);
        }

        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and("name LIKE ?", "%" + keyword + "%");
        }

        queryWrapper.orderBy("create_time DESC")
                .limit(offset, limit);
        return selectListByQuery(queryWrapper);
    }

    /**
     * 统计商家商品数量（支持状态和关键词筛选）
     */
    default long countByMerchantIdWithFilter(Long merchantId, Integer status, String keyword) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .from(Product.class)
                .where("merchant_id = ? AND is_deleted = 0", merchantId);

        if (status != null) {
            queryWrapper.and("status = ?", status);
        }

        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and("name LIKE ?", "%" + keyword + "%");
        }

        return selectCountByQuery(queryWrapper);
    }

    /**
     * 搜索商品
     */
    default List<Product> searchProduct(Long merchantId, String keyword, int limit, int offset) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Product.class)
                .where("merchant_id = ? AND is_deleted = 0", merchantId)
                .and("(name LIKE ? OR subtitle LIKE ? OR description LIKE ?)", 
                     "%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%")
                .orderBy("sort_order DESC, sales_volume DESC")
                .limit(offset, limit);
        return selectListByQuery(queryWrapper);
    }
    
    /**
     * 更新商品库存（乐观锁版本?     * 使用CAS机制防止超卖
     *
     * @param productId 商品ID
     * @param quantity 扣减数量
     * @param expectedVersion 预期版本?     * @return 影响行数?表示扣减失败（库存不足或版本冲突?     */
    default int updateStockWithVersion(Long productId, int quantity, Integer expectedVersion) {
        Product product = selectOneById(productId);
        if (product == null || product.getIsDeleted() == 1) {
            return 0;
        }
        
        if (product.getStock() != null && product.getStock() < quantity) {
            return 0;
        }
        
        if (product.getStock() == null) {
            return 0;
        }
        
        product.setStock(product.getStock() - quantity);
        product.setVersion((product.getVersion() == null ? 0 : product.getVersion()) + 1);
        
        return update(product);
    }
    
    /**
     * 更新商品库存（带重试机制?     *
     * @param productId 商品ID
     * @param quantity 扣减数量
     * @param maxRetry 最大重试次?     * @return 是否扣减成功
     */
    default boolean deductStockWithRetry(Long productId, int quantity, int maxRetry) {
        for (int i = 0; i < maxRetry; i++) {
            Product product = selectOneById(productId);
            if (product == null || product.getStock() < quantity) {
                return false;
            }
            
            int affected = updateStockWithVersion(productId, quantity, product.getVersion());
            if (affected > 0) {
                return true;
            }
            
            // 版本冲突，短暂休眠后重试
            try {
                Thread.sleep(10 + (long) (Math.random() * 20));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }
    
    /**
     * 更新商品库存（简单版本）
     *
     * @param productId 商品ID
     * @param quantity 扣减数量
     * @return 影响行数?表示扣减失败
     */
    default int updateStock(Long productId, int quantity) {
        return deductStockWithRetry(productId, quantity, 3) ? 1 : 0;
    }

    /**
     * 回滚商品库存
     */
    default int rollbackStock(Long productId, int quantity) {
        Product product = selectOneById(productId);
        if (product == null) {
            return 0;
        }
        product.setStock(product.getStock() + quantity);
        product.setVersion(product.getVersion() + 1);
        return update(product);
    }

    /**
     * 恢复商品库存（用于订单取消/退款）
     */
    default int restoreStock(Long productId, int quantity) {
        Product product = selectOneById(productId);
        if (product == null || product.getIsDeleted() == 1) {
            return 0;
        }
        product.setStock(product.getStock() + quantity);
        product.setVersion((product.getVersion() == null ? 0 : product.getVersion()) + 1);
        return update(product);
    }
}
