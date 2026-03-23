package com.xingchen.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.xingchen.backend.entity.Coupon;
import com.xingchen.backend.entity.UserCoupon;
import com.xingchen.backend.mapper.CouponMapper;
import com.xingchen.backend.mapper.UserCouponMapper;
import com.xingchen.backend.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 优惠券控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
@Tag(name = "优惠券", description = "优惠券相关接口")
public class CouponController {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;

    /**
     * 获取优惠券列表（可领取的）
     */
    @GetMapping("/list")
    @Operation(summary = "获取优惠券列表", description = "获取可领取的优惠券列表")
    public Result<List<Coupon>> getCouponList() {
        List<Coupon> coupons = couponMapper.selectValidCoupons();
        return Result.success("查询成功", coupons);
    }

    /**
     * 获取我的优惠券
     */
    @SaCheckLogin
    @GetMapping("/my")
    @Operation(summary = "获取我的优惠券", description = "获取当前用户已领取的优惠券")
    public Result<List<Map<String, Object>>> getMyCoupons(
            @Parameter(description = "状态：1-未使用，2-已使用，3-已过期") @RequestParam(required = false) Integer status) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        List<UserCoupon> userCoupons = userCouponMapper.selectByUserId(userId, status);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (UserCoupon userCoupon : userCoupons) {
            // 跳过 couponId 为 null 的记录
            if (userCoupon.getCouponId() == null) {
                continue;
            }
            Coupon coupon = couponMapper.selectOneById(userCoupon.getCouponId());
            if (coupon != null) {
                java.util.HashMap<String, Object> map = new java.util.HashMap<>();
                map.put("userCouponId", userCoupon.getId());
                map.put("couponId", coupon.getId());
                map.put("name", coupon.getName());
                map.put("type", coupon.getType());
                map.put("minAmount", coupon.getMinAmount());
                map.put("discountAmount", coupon.getDiscountAmount());
                map.put("discountRate", coupon.getDiscountRate());
                map.put("startTime", coupon.getStartTime());
                map.put("endTime", coupon.getEndTime());
                map.put("status", userCoupon.getStatus());
                map.put("description", coupon.getDescription());
                result.add(map);
            }
        }
        
        return Result.success("查询成功", result);
    }

    /**
     * 领取优惠券
     */
    @SaCheckLogin
    @PostMapping("/receive/{couponId}")
    @Operation(summary = "领取优惠券", description = "用户领取优惠券")
    public Result<Void> receiveCoupon(
            @Parameter(description = "优惠券ID") @PathVariable Long couponId) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        Coupon coupon = couponMapper.selectOneById(couponId);
        if (coupon == null) {
            return Result.error("优惠券不存在");
        }
        
        if (coupon.getStatus() != 1) {
            return Result.error("优惠券已下架");
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStartTime() != null && now.isBefore(coupon.getStartTime())) {
            return Result.error("优惠券还未开始");
        }
        if (coupon.getEndTime() != null && now.isAfter(coupon.getEndTime())) {
            return Result.error("优惠券已过期");
        }
        
        if (coupon.getRemainingQuantity() == null || coupon.getRemainingQuantity() <= 0) {
            return Result.error("优惠券已领完");
        }
        
        Long receivedCount = userCouponMapper.countByUserAndCoupon(userId, couponId);
        if (coupon.getPerLimit() != null && coupon.getPerLimit() > 0) {
            if (receivedCount != null && receivedCount >= coupon.getPerLimit()) {
                return Result.error("您已达到领取上限");
            }
        }
        
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setStatus(1);
        userCoupon.setCreateTime(now);
        userCouponMapper.insert(userCoupon);
        
        couponMapper.decreaseStock(couponId);
        
        return Result.success("领取成功", null);
    }

    /**
     * 获取可用优惠券（下单时）
     */
    @SaCheckLogin
    @GetMapping("/available")
    @Operation(summary = "获取可用优惠券", description = "获取当前用户可用于下单的优惠券")
    public Result<List<Map<String, Object>>> getAvailableCoupons(
            @Parameter(description = "商户ID") @RequestParam(required = false) Long merchantId,
            @Parameter(description = "订单金额") @RequestParam(required = false) java.math.BigDecimal orderAmount) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        List<UserCoupon> userCoupons = userCouponMapper.selectUnusedByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        
        LocalDateTime now = LocalDateTime.now();
        
        for (UserCoupon userCoupon : userCoupons) {
            Coupon coupon = couponMapper.selectOneById(userCoupon.getCouponId());
            if (coupon == null) continue;
            
            if (coupon.getEndTime() != null && now.isAfter(coupon.getEndTime())) {
                userCoupon.setStatus(3);
                userCouponMapper.update(userCoupon);
                continue;
            }
            
            if (orderAmount != null && coupon.getMinAmount() != null) {
                if (orderAmount.compareTo(coupon.getMinAmount()) < 0) {
                    continue;
                }
            }
            
            java.util.HashMap<String, Object> map = new java.util.HashMap<>();
            map.put("userCouponId", userCoupon.getId());
            map.put("couponId", coupon.getId());
            map.put("name", coupon.getName());
            map.put("type", coupon.getType());
            map.put("minAmount", coupon.getMinAmount());
            map.put("discountAmount", coupon.getDiscountAmount());
            map.put("discountRate", coupon.getDiscountRate());
            map.put("endTime", coupon.getEndTime());
            map.put("description", coupon.getDescription());
            result.add(map);
        }
        
        return Result.success("查询成功", result);
    }
}
