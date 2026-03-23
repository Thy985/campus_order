package com.xingchen.backend.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xingchen.backend.entity.Review;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 评价 Mapper
 */
@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    /**
     * 根据商家ID查询评价列表
     */
    default List<Review> selectByMerchantId(Long merchantId, int limit, int offset) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Review.class)
                .where("merchant_id = ? AND is_show = 1 AND is_deleted = 0", merchantId)
                .orderBy("create_time DESC")
                .limit(offset, limit);
        return selectListByQuery(queryWrapper);
    }

    /**
     * 根据订单ID查询评价
     */
    default Review selectByOrderId(Long orderId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Review.class)
                .where("order_id = ? AND is_deleted = 0", orderId);
        return selectOneByQuery(queryWrapper);
    }

    /**
     * 统计商家评分
     */
    default Double calculateAverageRating(Long merchantId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select("AVG(rating)")
                .from(Review.class)
                .where("merchant_id = ? AND is_show = 1 AND is_deleted = 0", merchantId);
        Object result = selectObjectByQuery(queryWrapper);
        return result != null ? ((Number) result).doubleValue() : 5.0;
    }

    /**
     * 统计商家评价数量
     */
    default Long countByMerchantId(Long merchantId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .from(Review.class)
                .where("merchant_id = ? AND is_show = 1 AND is_deleted = 0", merchantId);
        return selectCountByQuery(queryWrapper);
    }
}
