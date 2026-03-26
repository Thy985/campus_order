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
    private Long id;
    
    private String paymentNo;
    
    private Long orderId;
    
    private String orderNo;
    
    private Long userId;
    
    private BigDecimal amount;
    
    private Integer channel;
    
    private String tradeNo;
    
    private Integer status;
    
    private LocalDateTime payTime;
    
    private LocalDateTime callbackTime;
    
    private Integer version;
    
    private Integer isDeleted;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
