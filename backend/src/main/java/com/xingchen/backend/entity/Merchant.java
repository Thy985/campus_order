package com.xingchen.backend.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Column;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table("merchant")
public class Merchant implements Serializable {

    @Id(keyType = KeyType.Auto)
    @Column("id")
    private Long id;
    
    @Column("name")
    private String name;
    
    @Column("logo")
    private String logo;
    
    @Column("banner")
    private String banner;
    
    @Column("category_id")
    private Integer categoryId;
    
    @Column("description")
    private String description;
    
    @Column("notice")
    private String notice;
    
    @Column("phone")
    private String phone;
    
    @Column("avg_price")
    private BigDecimal avgPrice;
    
    @Column("rating")
    private BigDecimal rating;
    
    @Column("sales_volume")
    private Integer salesVolume;
    
    @Column("status")
    private Integer status;
    
    @Column("sort_order")
    private Integer sortOrder;

    @Column("is_deleted")
    private Integer isDeleted;

    @Column("address")
    private String address;

    @Column("min_order_amount")
    private BigDecimal minOrderAmount;

    @Column("create_time")
    private LocalDateTime createTime;

    @Column("update_time")
    private LocalDateTime updateTime;
}
