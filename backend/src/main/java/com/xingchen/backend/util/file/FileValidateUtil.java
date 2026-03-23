package com.xingchen.backend.util.file;

import cn.hutool.core.io.FileUtil;
import com.xingchen.backend.config.FileConfig;
import com.xingchen.backend.util.constant.ErrorCode;
import com.xingchen.backend.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * 文件验证工具类
 * 
 * @author 小跃
 * @date 2026-02-13
 */
@Slf4j
public class FileValidateUtil {
    
    /**
     * 允许的图片扩展名
     */
    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    );
    
    /**
     * 允许的文档扩展名
     */
    private static final List<String> DOCUMENT_EXTENSIONS = Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt"
    );
    
    /**
     * 允许的视频扩展名
     */
    private static final List<String> VIDEO_EXTENSIONS = Arrays.asList(
            "mp4", "avi", "mov", "wmv", "flv", "mkv"
    );
    
    /**
     * 危险文件扩展名（不允许上传）
     */
    private static final List<String> DANGEROUS_EXTENSIONS = Arrays.asList(
            "exe", "bat", "cmd", "sh", "ps1", "vbs", "js", "jar", "war"
    );
    
    /**
     * 验证文件
     * 
     * @param file 上传文件
     * @param fileConfig 文件配置
     * @throws BusinessException 验证失败时抛出异常
     */
    public static void validateFile(MultipartFile file, FileConfig fileConfig) {
        // 1. 验证文件是否为空
        validateNotEmpty(file);
        
        // 2. 验证文件大小
        validateFileSize(file, fileConfig.getMaxSize());
        
        // 3. 验证文件扩展名
        validateFileExtension(file, fileConfig.getAllowedExtensions());
        
        // 4. 验证文件类型（MIME Type）
        validateMimeType(file);
        
        // 5. 验证文件名
        validateFileName(file);
    }
    
    /**
     * 验证文件不为空
     * 
     * @param file 上传文件
     */
    private static void validateNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件不能为空");
        }
    }
    
    /**
     * 验证文件大小
     * 
     * @param file 上传文件
     * @param maxSize 最大大小（字节）
     */
    private static void validateFileSize(MultipartFile file, long maxSize) {
        long fileSize = file.getSize();
        
        if (fileSize > maxSize) {
            long maxSizeMB = maxSize / 1024 / 1024;
            throw new BusinessException(ErrorCode.PARAM_ERROR, 
                    String.format("文件大小超过限制，最大允许%dMB", maxSizeMB));
        }
        
        if (fileSize == 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件大小不能为空");
        }
    }
    
    /**
     * 验证文件扩展名
     * 
     * @param file 上传文件
     * @param allowedExtensions 允许的扩展名（逗号分隔）
     */
    private static void validateFileExtension(MultipartFile file, String allowedExtensions) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件名不能为空");
        }
        
        String extension = FileUtil.extName(originalFilename).toLowerCase();
        
        // 检查是否为危险文件
        if (DANGEROUS_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, 
                    String.format("不允许上传%s类型的文件", extension));
        }
        
        // 检查是否在允许的扩展名列表中
        List<String> allowedList = Arrays.asList(allowedExtensions.split(","));
        if (!allowedList.contains(extension)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, 
                    String.format("不支持的文件类型，仅支持%s", allowedExtensions));
        }
    }
    
    /**
     * 验证MIME类型
     * 
     * @param file 上传文件
     */
    private static void validateMimeType(MultipartFile file) {
        String contentType = file.getContentType();
        
        if (contentType == null || contentType.isEmpty()) {
            log.warn("文件MIME类型为空: fileName={}", file.getOriginalFilename());
            return;
        }
        
        // 验证MIME类型与文件扩展名是否匹配
        String extension = FileUtil.extName(file.getOriginalFilename()).toLowerCase();
        
        if (IMAGE_EXTENSIONS.contains(extension)) {
            if (!contentType.startsWith("image/")) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, 
                        "文件类型不匹配，期望图片类型");
            }
        } else if (DOCUMENT_EXTENSIONS.contains(extension)) {
            // 文档类型验证（可选）
            log.debug("文档类型验证: contentType={}", contentType);
        } else if (VIDEO_EXTENSIONS.contains(extension)) {
            if (!contentType.startsWith("video/")) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, 
                        "文件类型不匹配，期望视频类型");
            }
        }
    }
    
    /**
     * 验证文件名
     * 
     * @param file 上传文件
     */
    private static void validateFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件名不能为空");
        }
        
        // 验证文件名长度
        if (originalFilename.length() > 255) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件名过长，最多255个字符");
        }
        
        // 验证文件名是否包含非法字符
        String illegalChars = "[\\\\/:*?\"<>|]";
        if (originalFilename.matches(".*" + illegalChars + ".*")) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, 
                    "文件名包含非法字符：\\ / : * ? \" < > |");
        }
    }
    
    /**
     * 判断是否为图片
     * 
     * @param file 上传文件
     * @return 是否为图片
     */
    public static boolean isImage(MultipartFile file) {
        String extension = FileUtil.extName(file.getOriginalFilename()).toLowerCase();
        return IMAGE_EXTENSIONS.contains(extension);
    }
    
    /**
     * 判断是否为文档
     * 
     * @param file 上传文件
     * @return 是否为文档
     */
    public static boolean isDocument(MultipartFile file) {
        String extension = FileUtil.extName(file.getOriginalFilename()).toLowerCase();
        return DOCUMENT_EXTENSIONS.contains(extension);
    }
    
    /**
     * 判断是否为视频
     * 
     * @param file 上传文件
     * @return 是否为视频
     */
    public static boolean isVideo(MultipartFile file) {
        String extension = FileUtil.extName(file.getOriginalFilename()).toLowerCase();
        return VIDEO_EXTENSIONS.contains(extension);
    }
    
    /**
     * 获取文件扩展名
     * 
     * @param file 上传文件
     * @return 扩展名（小写）
     */
    public static String getExtension(MultipartFile file) {
        return FileUtil.extName(file.getOriginalFilename()).toLowerCase();
    }
    
    /**
     * 格式化文件大小
     * 
     * @param size 文件大小（字节）
     * @return 格式化后的大小（如：1.5MB）
     */
    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + "B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2fKB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2fMB", size / 1024.0 / 1024.0);
        } else {
            return String.format("%.2fGB", size / 1024.0 / 1024.0 / 1024.0);
        }
    }
}
