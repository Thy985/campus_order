package com.xingchen.backend.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Column;
import lombok.Data;

import java.io.Serializable;

@Data
@Table("order_status")
public class OrderStatus implements Serializable {
    
    @Id
    @Column("id")
    private Integer id;
    
    @Column("name")
    private String name;
    
    @Column("sort_order")
    private Integer sortOrder;
    
    @Column("status")
    private Integer status;
    
    @Column("is_deleted")
    private Integer isDeleted;
}
