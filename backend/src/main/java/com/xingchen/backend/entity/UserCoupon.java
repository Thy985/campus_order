package com.xingchen.backend.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Column;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户优惠券实体
 */
@Data
@Table("user_coupon")
public class UserCoupon implements Serializable {

    @Id(keyType = KeyType.Auto)
    @Column("id")
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("coupon_id")
    private Long couponId;

    @Column("status")
    private Integer status;

    @Column("use_time")
    private LocalDateTime useTime;

    @Column("order_id")
    private Long orderId;

    @Column("is_deleted")
    private Integer isDeleted;

    @Column("create_time")
    private LocalDateTime createTime;
}
