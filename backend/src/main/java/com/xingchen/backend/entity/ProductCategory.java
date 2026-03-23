package com.xingchen.backend.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Column;
import lombok.Data;

import java.io.Serializable;

@Data
@Table("product_category")
public class ProductCategory implements Serializable {

    @Id(keyType = KeyType.Auto)
    @Column("id")
    private Integer id;
    
    @Column("name")
    private String name;
    
    @Column("parent_id")
    private Integer parentId;
    
    @Column("sort_order")
    private Integer sortOrder;
    
    @Column("status")
    private Integer status;
    
    @Column("is_deleted")
    private Integer isDeleted;
    
    @Column("level")
    private Integer level;
}
