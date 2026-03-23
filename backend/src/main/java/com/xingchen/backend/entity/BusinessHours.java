package com.xingchen.backend.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Column;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Table("merchant_business_hours")
public class BusinessHours implements Serializable {

    @Id(keyType = KeyType.Auto)
    @Column("id")
    private Long id;
    
    @Column("merchant_id")
    private Long merchantId;
    
    @Column("day_of_week")
    private Integer dayOfWeek;
    
    @Column("meal_type")
    private Integer mealType;
    
    @Column("start_time")
    private String startTime;
    
    @Column("end_time")
    private String endTime;
    
    @Column("is_open")
    private Integer isOpen;
    
    @Column("is_deleted")
    private Integer isDeleted;
    
    @Column("create_time")
    private LocalDateTime createTime;
    
    @Column("update_time")
    private LocalDateTime updateTime;
    
    public Integer getStatus() {
        return isOpen;
    }
    
    public void setStatus(Integer status) {
        this.isOpen = status;
    }
}
