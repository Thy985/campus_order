package com.xingchen.backend.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xingchen.backend.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

/**
 * 优惠券Mapper
 */
@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {

    /**
     * 查询有效的优惠券列表
     */
    default List<Coupon> selectValidCoupons() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Coupon.class)
                .where("status = 1")
                .and("is_deleted = 0")
                .and("start_time <= ?", new Date())
                .and("end_time >= ?", new Date())
                .and("remaining_quantity > 0")
                .orderBy("create_time DESC");
        return selectListByQuery(queryWrapper);
    }

    /**
     * 根据类型查询优惠券
     */
    default List<Coupon> selectByType(Integer type) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Coupon.class)
                .where("type = ?", type)
                .and("status = 1")
                .and("is_deleted = 0")
                .orderBy("create_time DESC");
        return selectListByQuery(queryWrapper);
    }

    /**
     * 查询用户可领取的优惠券
     */
    default List<Coupon> selectAvailableCoupons(Long userId) {
        // 查询用户已领取的优惠券ID
        QueryWrapper userCouponQuery = QueryWrapper.create()
                .select("coupon_id")
                .from("user_coupon")
                .where("user_id = ?", userId);

        // 查询可领取的优惠券（排除已领取的）
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Coupon.class)
                .where("status = 1")
                .and("is_deleted = 0")
                .and("start_time <= ?", new Date())
                .and("end_time >= ?", new Date())
                .and("remaining_quantity > 0")
                .orderBy("create_time DESC");

        return selectListByQuery(queryWrapper);
    }

    /**
     * 减少优惠券库存
     */
    default int decreaseStock(Long couponId) {
        Coupon coupon = selectOneById(couponId);
        if (coupon != null && coupon.getRemainingQuantity() != null && coupon.getRemainingQuantity() > 0) {
            coupon.setRemainingQuantity(coupon.getRemainingQuantity() - 1);
            return update(coupon);
        }
        return 0;
    }

    /**
     * 增加优惠券库存
     */
    default int increaseStock(Long couponId) {
        Coupon coupon = selectOneById(couponId);
        if (coupon != null && coupon.getRemainingQuantity() != null) {
            coupon.setRemainingQuantity(coupon.getRemainingQuantity() + 1);
            return update(coupon);
        }
        return 0;
    }

    /**
     * 查询即将过期的优惠券
     */
    default List<Coupon> selectExpiringCoupons(int days) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.add(java.util.Calendar.DAY_OF_YEAR, days);
        Date expireDate = calendar.getTime();

        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Coupon.class)
                .where("status = 1")
                .and("is_deleted = 0")
                .and("end_time <= ?", expireDate)
                .and("end_time >= ?", new Date())
                .orderBy("end_time ASC");

        return selectListByQuery(queryWrapper);
    }

    /**
     * 根据状态查询优惠券
     */
    default List<Coupon> selectByStatus(Integer status) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(Coupon.class)
                .where("status = ?", status)
                .and("is_deleted = 0")
                .orderBy("create_time DESC");
        return selectListByQuery(queryWrapper);
    }

    /**
     * 查询优惠券总数
     */
    default long countValidCoupons() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .from(Coupon.class)
                .where("status = 1")
                .and("is_deleted = 0")
                .and("start_time <= ?", new Date())
                .and("end_time >= ?", new Date())
                .and("remaining_quantity > 0");
        return selectCountByQuery(queryWrapper);
    }
}
