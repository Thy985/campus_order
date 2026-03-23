package com.xingchen.backend.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Column;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价实体
 */
@Data
@Table("review")
public class Review implements Serializable {

    @Id(keyType = KeyType.Auto)
    @Column("id")
    private Long id;

    @Column("order_id")
    private Long orderId;

    @Column("user_id")
    private Long userId;

    @Column("merchant_id")
    private Long merchantId;

    @Column("rating")
    private Integer rating;

    @Column("content")
    private String content;

    @Column("images")
    private String images;

    @Column("merchant_reply")
    private String merchantReply;

    @Column("reply_time")
    private LocalDateTime replyTime;
    

    @Column("is_show")
    private Integer isShow;

    @Column("is_deleted")
    private Integer isDeleted;

    @Column("create_time")
    private LocalDateTime createTime;

    @Column("update_time")
    private LocalDateTime updateTime;
}
