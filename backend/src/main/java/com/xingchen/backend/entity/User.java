package com.xingchen.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Column;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Table("user")
public class User implements Serializable {

    @Id(keyType = KeyType.Auto)
    @Column("id")
    private Long id;
    
    @Column("username")
    private String username;
    
    @Column("nickname")
    private String nickname;
    
    @Column("phone")
    private String phone;
    
    @Column("email")
    private String email;
    
    /**
     * 密码哈希值，序列化时忽略，防止泄露到前端
     */
    @JsonIgnore
    @Column("password")
    private String password;
    
    @Column("avatar")
    private String avatar;
    
    @Column("gender")
    private Integer gender;
    
    @Column("user_type")
    private Integer userType;
    
    @Column("merchant_id")
    private Long merchantId;
    
    @Column("status")
    private Integer status;
    
    @Column("last_login_time")
    private LocalDateTime lastLoginTime;
    
    @Column("last_login_ip")
    private String lastLoginIp;
    
    @Column("login_count")
    private Integer loginCount;
    
    @Column("register_time")
    private LocalDateTime registerTime;
    
    @Column("register_ip")
    private String registerIp;
    
    @Column("password_modify_time")
    private LocalDateTime passwordModifyTime;
    
    @Column("is_deleted")
    private Integer isDeleted;
    
    @Column("create_time")
    private LocalDateTime createTime;
    
    @Column("update_time")
    private LocalDateTime updateTime;
}
