package com.xingchen.backend.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Column;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息通知实体
 */
@Data
@Table("notification")
public class Notification implements Serializable {

    @Id(keyType = KeyType.Auto)
    @Column("id")
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("type")
    private Integer type;

    @Column("title")
    private String title;

    @Column("content")
    private String content;

    @Column("extra_data")
    private String extraData;

    @Column("is_read")
    private Integer isRead;

    @Column("is_deleted")
    private Integer isDeleted;

    @Column("create_time")
    private LocalDateTime createTime;
}
