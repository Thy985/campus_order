package com.xingchen.backend.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.xingchen.backend.config.FileConfig;
import com.xingchen.backend.entity.File;
import com.xingchen.backend.mapper.FileMapper;
import com.xingchen.backend.service.FileService;
import com.xingchen.backend.util.constant.Constants;
import com.xingchen.backend.util.constant.ErrorCode;
import com.xingchen.backend.exception.BusinessException;
import com.xingchen.backend.util.file.FileValidateUtil;
import com.xingchen.backend.util.file.ImageCompressUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 文件服务实现
 * 
 * @author 小跃
 * @date 2026-02-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    
    private final FileConfig fileConfig;
    private final FileMapper fileMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadFile(MultipartFile file, Integer fileType, Long userId) {
        log.info("开始上传文件: fileName={}, fileType={}, userId={}, size={}KB",
                file.getOriginalFilename(), fileType, userId, file.getSize() / 1024);
        
        try {
            // 1. 验证文件
            FileValidateUtil.validateFile(file, fileConfig);
            
            // 2. 生成文件路径
            String filePath = generateFilePath(file, fileType);
            
            // 3. 保存文件
            java.io.File targetFile = saveFile(file, filePath);
            
            // 4. 如果是图片，进行压缩
            if (FileValidateUtil.isImage(file)) {
                compressImage(targetFile);
            }
            
            // 5. 保存文件记录到数据库
            File fileEntity = saveFileRecord(file, filePath, fileType, userId, targetFile);
            
            // 6. 生成访问URL
            String accessUrl = generateAccessUrl(filePath);
            
            log.info("文件上传成功: fileId={}, accessUrl={}", fileEntity.getId(), accessUrl);
            
            return accessUrl;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long fileId, Long userId) {
        log.info("开始删除文件: fileId={}, userId={}", fileId, userId);
        
        try {
            // 1. 查询文件记录
            File file = fileMapper.selectOneById(fileId);
            
            if (file == null || file.getIsDeleted() == Constants.DeleteFlag.DELETED) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件不存在");
            }
            
            // 2. 验证权限（只能删除自己上传的文件）
            if (!file.getUploadUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "无权限删除此文件");
            }
            
            // 3. 删除物理文件
            String fullPath = fileConfig.getUploadDir() + java.io.File.separator + file.getFilePath();
            java.io.File physicalFile = new java.io.File(fullPath);
            
            if (physicalFile.exists()) {
                boolean deleted = physicalFile.delete();
                if (deleted) {
                    log.info("物理文件删除成功: path={}", fullPath);
                } else {
                    log.warn("物理文件删除失败: path={}", fullPath);
                }
            }
            
            // 4. 逻辑删除数据库记录
            file.setIsDeleted(Constants.DeleteFlag.DELETED);
            fileMapper.update(file);
            
            log.info("文件删除成功: fileId={}", fileId);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件删除失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件删除失败: " + e.getMessage());
        }
    }
    
    @Override
    public File getFileInfo(Long fileId) {
        File file = fileMapper.selectOneById(fileId);
        
        if (file == null || file.getIsDeleted() == Constants.DeleteFlag.DELETED) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件不存在");
        }
        
        return file;
    }
    
    /**
     * 生成文件路径
     * 
     * 路径结构：fileType/yyyy/MM/dd/uuid.ext
     * 示例：avatar/2026/02/13/abc123.jpg
     * 
     * @param file 上传文件
     * @param fileType 文件类型
     * @return 文件路径
     */
    private String generateFilePath(MultipartFile file, Integer fileType) {
        // 获取文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = FileUtil.extName(originalFilename);
        
        // 生成UUID文件名
        String fileName = IdUtil.simpleUUID() + "." + extension;
        
        // 获取文件类型目录
        String typeDir = getFileTypeDir(fileType);
        
        // 获取日期目录（yyyy/MM/dd）
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        
        // 组合路径
        return typeDir + java.io.File.separator + dateDir + java.io.File.separator + fileName;
    }
    
    /**
     * 获取文件类型目录
     * 
     * @param fileType 文件类型
     * @return 目录名
     */
    private String getFileTypeDir(Integer fileType) {
        switch (fileType) {
            case 1:
                return "avatar";
            case 2:
                return "product";
            case 3:
                return "merchant";
            default:
                return "other";
        }
    }
    
    /**
     * 保存文件到磁盘
     *
     * @param file 上传文件
     * @param filePath 文件路径
     * @return 保存的文件对象
     * @throws IOException IO异常
     */
    private java.io.File saveFile(MultipartFile file, String filePath) throws IOException {
        // 完整路径
        String fullPath = fileConfig.getUploadDir() + java.io.File.separator + filePath;
        java.io.File targetFile = new java.io.File(fullPath);
        
        // 创建目录
        java.io.File parentDir = targetFile.getParentFile();
        if (!parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created) {
                throw new IOException("创建目录失败: " + parentDir.getPath());
            }
        }
        
        // 保存文件
        file.transferTo(targetFile);
        
        log.debug("文件保存成功: path={}, size={}KB", fullPath, targetFile.length() / 1024);
        
        return targetFile;
    }
    
    /**
     * 压缩图片
     * 
     * @param file 图片文件
     */
    private void compressImage(java.io.File file) {
        try {
            long originalSize = file.length();
            
            // 如果文件小于100KB，不压缩
            if (originalSize < 100 * 1024) {
                log.debug("图片小于100KB，跳过压缩: size={}KB", originalSize / 1024);
                return;
            }
            
            // 创建临时文件
            java.io.File tempFile = new java.io.File(file.getPath() + ".tmp");
            
            // 智能压缩
            ImageCompressUtil.smartCompress(file, tempFile);
            
            // 替换原文件
            boolean deleted = file.delete();
            boolean renamed = tempFile.renameTo(file);
            
            if (deleted && renamed) {
                long compressedSize = file.length();
                double ratio = (1 - (double) compressedSize / originalSize) * 100;
                log.info("图片压缩成功: 原始={}KB, 压缩后={}KB, 压缩率={}%",
                        originalSize / 1024, compressedSize / 1024, String.format("%.2f", ratio));
            } else {
                log.warn("图片压缩失败：文件替换失败");
            }
            
        } catch (Exception e) {
            log.error("图片压缩失败", e);
            // 压缩失败不影响上传，继续使用原图
        }
    }
    
    /**
     * 保存文件记录到数据库
     * 
     * @param multipartFile 上传文件
     * @param filePath 文件路径
     * @param fileType 文件类型
     * @param userId 用户ID
     * @param savedFile 已保存的文件
     * @return 文件实体
     */
    private File saveFileRecord(MultipartFile multipartFile, String filePath, 
                                Integer fileType, Long userId, java.io.File savedFile) {
        File file = new File();
        file.setFileName(multipartFile.getOriginalFilename());
        file.setFilePath(filePath);
        file.setFileSize(savedFile.length());
        file.setFileType(fileType.toString());
        file.setUploadUserId(userId);
        file.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        file.setCreateTime(LocalDateTime.now());

        fileMapper.insert(file);
        
        return file;
    }
    
    /**
     * 生成访问URL
     * 
     * @param filePath 文件路径
     * @return 访问URL
     */
    private String generateAccessUrl(String filePath) {
        return fileConfig.getAccessUrl() + "/" + filePath.replace("\\", "/");
    }
}
