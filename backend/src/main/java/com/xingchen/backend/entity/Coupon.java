package com.xingchen.backend.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Column;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券实体
 */
@Data
@Table("coupon")
public class Coupon implements Serializable {

    @Id(keyType = KeyType.Auto)
    @Column("id")
    private Long id;

    @Column("name")
    private String name;

    @Column("type")
    private Integer type;

    @Column("min_amount")
    private BigDecimal minAmount;

    @Column("discount_amount")
    private BigDecimal discountAmount;

    @Column("discount_rate")
    private BigDecimal discountRate;

    @Column("total_quantity")
    private Integer totalQuantity;

    @Column("remaining_quantity")
    private Integer remainingQuantity;

    @Column("per_limit")
    private Integer perLimit;

    @Column("start_time")
    private LocalDateTime startTime;

    @Column("end_time")
    private LocalDateTime endTime;

    @Column("status")
    private Integer status;

    @Column("description")
    private String description;

    @Column("is_deleted")
    private Integer isDeleted;

    @Column("create_time")
    private LocalDateTime createTime;

    @Column("update_time")
    private LocalDateTime updateTime;
}
