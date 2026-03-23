package com.xingchen.backend.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Column;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收货地址实体
 */
@Data
@Table("address")
public class Address implements Serializable {

    @Id(keyType = KeyType.Auto)
    @Column("id")
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("contact_name")
    private String contactName;

    @Column("contact_phone")
    private String contactPhone;

    @Column("province")
    private String province;

    @Column("city")
    private String city;

    @Column("district")
    private String district;

    @Column("detail_address")
    private String detailAddress;

    @Column("is_default")
    private Integer isDefault;

    @Column("is_deleted")
    private Integer isDeleted;

    @Column("create_time")
    private LocalDateTime createTime;

    @Column("update_time")
    private LocalDateTime updateTime;
}
