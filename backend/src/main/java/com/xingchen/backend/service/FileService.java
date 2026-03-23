package com.xingchen.backend.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口
 */
public interface FileService {
    
    /**
     * 上传文件
     * @param file 文件
     * @param fileType 文件类型 (1-头像, 2-商品图片, 3-商家图片)
     * @param userId 上传用户ID
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, Integer fileType, Long userId);
    
    /**
     * 删除文件
     * @param fileId 文件ID
     * @param userId 用户ID
     */
    void deleteFile(Long fileId, Long userId);
    
    /**
     * 获取文件信息
     * @param fileId 文件ID
     * @return 文件信息
     */
    com.xingchen.backend.entity.File getFileInfo(Long fileId);
}
