package com.xingchen.backend.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Column;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Table("merchant_category")
public class MerchantCategory implements Serializable {

    @Id(keyType = KeyType.Auto)
    @Column("id")
    private Integer id;
    
    @Column("name")
    private String name;
    
    @Column("icon")
    private String icon;
    
    @Column("sort_order")
    private Integer sortOrder;
    
    @Column("is_deleted")
    private Integer isDeleted;
    
    @Column("create_time")
    private LocalDateTime createTime;
    
    @Column("update_time")
    private LocalDateTime updateTime;
}
