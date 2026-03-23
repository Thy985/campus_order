package com.xingchen.backend.mapper;

import com.mybatisflex.core.BaseMapper;
import com.xingchen.backend.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户优惠券Mapper
 */
@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {

    /**
     * 插入用户优惠券
     */
    int insert(UserCoupon userCoupon);

    /**
     * 更新用户优惠券
     */
    int update(UserCoupon userCoupon);

    /**
     * 查询用户未使用的优惠券
     */
    @Select("SELECT id, user_id, coupon_id, status, use_time, order_id, create_time, update_time, is_deleted FROM user_coupon WHERE user_id = #{userId} AND status = 1 AND is_deleted = 0")
    List<UserCoupon> selectUnusedByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和优惠券ID查询
     */
    @Select("SELECT COUNT(*) FROM user_coupon WHERE user_id = #{userId} AND coupon_id = #{couponId} AND is_deleted = 0")
    Long countByUserAndCoupon(@Param("userId") Long userId, @Param("couponId") Long couponId);

    /**
     * 查询用户已领取的优惠券
     */
    @Select({
        "<script>",
        "SELECT id, user_id, coupon_id, status, use_time, order_id, create_time, update_time, is_deleted FROM user_coupon WHERE user_id = #{userId} AND is_deleted = 0",
        "<if test='status != null'>AND status = #{status}</if>",
        "ORDER BY create_time DESC",
        "</script>"
    })
    List<UserCoupon> selectByUserId(@Param("userId") Long userId, @Param("status") Integer status);
}
