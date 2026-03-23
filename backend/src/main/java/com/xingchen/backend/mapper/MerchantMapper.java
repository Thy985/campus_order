package com.xingchen.backend.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xingchen.backend.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商家 Mapper
 */
@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {
    
    /**
     * 查询商家列表
     */
    default List<Merchant> selectMerchantList(Integer categoryId, Integer status, int limit, int offset) {
        return selectMerchantList(categoryId, status, null, limit, offset);
    }

    /**
     * 查询商家列表（支持关键词搜索）
     */
    default List<Merchant> selectMerchantList(Integer categoryId, Integer status, String keyword, int limit, int offset) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Merchant.class)
                .where("is_deleted = 0");

        if (categoryId != null) {
            queryWrapper.and("category_id = ?", categoryId);
        }

        if (status != null) {
            queryWrapper.and("status = ?", status);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and("(name LIKE ? OR description LIKE ?)", "%" + keyword.trim() + "%", "%" + keyword.trim() + "%");
        }

        queryWrapper.orderBy("sort_order DESC, sales_volume DESC, create_time DESC")
                .limit(offset, limit);

        return selectListByQuery(queryWrapper);
    }
    
    /**
     * 根据分类ID查询商家列表
     */
    default List<Merchant> selectByCategoryId(Integer categoryId, int limit, int offset) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Merchant.class)
                .where("category_id = ? AND is_deleted = 0", categoryId)
                .orderBy("sort_order DESC, create_time DESC")
                .limit(offset, limit);
        return selectListByQuery(queryWrapper);
    }
    
    /**
     * 根据状态查询商家列?     */
    default List<Merchant> selectByStatus(Integer status, int limit, int offset) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Merchant.class)
                .where("status = ? AND is_deleted = 0", status)
                .orderBy("sort_order DESC, create_time DESC")
                .limit(offset, limit);
        return selectListByQuery(queryWrapper);
    }
    
    /**
     * 搜索商家
     */
    default List<Merchant> searchMerchant(String keyword, int limit, int offset) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Merchant.class)
                .where("is_deleted = 0")
                .and("(name LIKE ? OR description LIKE ?)", "%" + keyword + "%", "%" + keyword + "%")
                .orderBy("sort_order DESC, sales_volume DESC")
                .limit(offset, limit);
        return selectListByQuery(queryWrapper);
    }
}
