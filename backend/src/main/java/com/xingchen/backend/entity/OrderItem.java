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
@Table("order_item")
public class OrderItem implements Serializable {

    @Id(keyType = KeyType.Auto)
    @Column("id")
    private Long id;
    
    @Column("order_id")
    private Long orderId;
    
    @Column("product_id")
    private Long productId;
    
    @Column("product_name")
    private String productName;
    
    @Column("price")
    private BigDecimal price;
    
    @Column("quantity")
    private Integer quantity;
    
    @Column("total_amount")
    private BigDecimal totalAmount;
    
    @Column("product_image")
    private String productImage;
    
    @Column("create_time")
    private LocalDateTime createTime;
}
