package com.xingchen.backend.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Column;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件实体
 */
@Data
@Table("file")
public class File implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    @Column("id")
    private Long id;
    
    @Column("file_name")
    private String fileName;
    
    @Column("file_type")
    private String fileType;
    
    @Column("file_size")
    private Long fileSize;
    
    @Column("file_path")
    private String filePath;
    
    @Column("file_url")
    private String fileUrl;
    
    @Column("upload_user_id")
    private Long uploadUserId;
    
    @Column("is_deleted")
    private Integer isDeleted;
    
    @Column("create_time")
    private LocalDateTime createTime;
}
