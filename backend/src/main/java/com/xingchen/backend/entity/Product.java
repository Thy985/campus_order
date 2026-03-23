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
@Table("product")
public class Product implements Serializable {

    @Id(keyType = KeyType.Auto)
    @Column("id")
    private Long id;
    
    @Column("merchant_id")
    private Long merchantId;
    
    @Column("category_id")
    private Integer categoryId;
    
    @Column("name")
    private String name;
    
    @Column("subtitle")
    private String subtitle;
    
    @Column("image")
    private String image;
    
    @Column("images")
    private String images;
    
    @Column("description")
    private String description;
    
    @Column("price")
    private BigDecimal price;
    
    @Column("original_price")
    private BigDecimal originalPrice;
    
    @Column("unit")
    private String unit;
    
    @Column("stock")
    private Integer stock;
    
    @Column("sales_volume")
    private Integer salesVolume;
    
    @Column("status")
    private Integer status;
    
    @Column("sort_order")
    private Integer sortOrder;
    
    @Column("is_deleted")
    private Integer isDeleted;
    
    @Column("version")
    private Integer version;
    
    @Column("create_time")
    private LocalDateTime createTime;
    
    @Column("update_time")
    private LocalDateTime updateTime;
}
