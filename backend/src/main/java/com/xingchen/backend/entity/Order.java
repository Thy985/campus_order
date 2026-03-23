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
@Table("order")
public class Order implements Serializable {

    @Id(keyType = KeyType.Auto)
    @Column("id")
    private Long id;
    
    @Column("order_no")
    private String orderNo;
    
    @Column("user_id")
    private Long userId;
    
    @Column("merchant_id")
    private Long merchantId;
    
    @Column("order_type")
    private Integer orderType;
    
    @Column("total_amount")
    private BigDecimal totalAmount;
    
    @Column("actual_amount")
    private BigDecimal actualAmount;
    
    @Column("remark")
    private String remark;

    @Column("delivery_address")
    private String deliveryAddress;

    @Column("contact_phone")
    private String contactPhone;

    @Column("contact_name")
    private String contactName;

    @Column("status")
    private Integer status;
    
    @Column("pay_status")
    private Integer payStatus;

    @Column("pay_method")
    private Integer payMethod;

    @Column("pay_time")
    private LocalDateTime payTime;
    
    @Column("accept_time")
    private LocalDateTime acceptTime;
    
    @Column("finish_time")
    private LocalDateTime finishTime;
    
    @Column("cancel_time")
    private LocalDateTime cancelTime;
    
    @Column("cancel_reason")
    private String cancelReason;
    
    @Column("is_deleted")
    private Integer isDeleted;
    
    @Column("is_archived")
    private Integer isArchived;
    
    @Column("archive_time")
    private LocalDateTime archiveTime;
    
    @Column("create_time")
    private LocalDateTime createTime;
    
    @Column("update_time")
    private LocalDateTime updateTime;
}
