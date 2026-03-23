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
 * 支付实体
 */
@Data
@Table("payment")
public class Payment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    @Column("id")
    private Long id;
    
    @Column("payment_no")
    private String paymentNo;
    
    @Column("order_id")
    private Long orderId;
    
    @Column("order_no")
    private String orderNo;
    
    @Column("user_id")
    private Long userId;
    
    @Column("amount")
    private BigDecimal amount;
    
    @Column("channel")
    private Integer channel;
    
    @Column("trade_no")
    private String tradeNo;
    
    @Column("status")
    private Integer status;
    
    @Column("pay_time")
    private LocalDateTime payTime;
    
    @Column("callback_time")
    private LocalDateTime callbackTime;
    
    @Column("version")
    private Integer version;
    
    @Column("is_deleted")
    private Integer isDeleted;
    
    @Column("create_time")
    private LocalDateTime createTime;
    
    @Column("update_time")
    private LocalDateTime updateTime;
}
