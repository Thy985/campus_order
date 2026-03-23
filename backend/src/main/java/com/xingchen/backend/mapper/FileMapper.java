package com.xingchen.backend.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import com.xingchen.backend.entity.File;

import java.util.List;

/**
 * 文件 Mapper
 */
@Mapper
public interface FileMapper extends BaseMapper<File> {
    
    /**
     * 根据上传用户ID查询文件列表
     */
    default List<com.xingchen.backend.entity.File> selectByUploadUserId(Long uploadUserId, int limit, int offset) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(com.xingchen.backend.entity.File.class)
                .where("upload_user_id = ? AND is_deleted = 0", uploadUserId)
                .orderBy("create_time DESC")
                .limit(offset, limit);
        return selectListByQuery(queryWrapper);
    }
    
    /**
     * 根据文件路径查询文件
     */
    default com.xingchen.backend.entity.File selectByFilePath(String filePath) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .from(com.xingchen.backend.entity.File.class)
                .where("file_path = ? AND is_deleted = 0", filePath);
        return selectOneByQuery(queryWrapper);
    }
}
