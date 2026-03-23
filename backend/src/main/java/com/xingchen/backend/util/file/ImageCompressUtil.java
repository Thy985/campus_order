package com.xingchen.backend.util.file;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 图片压缩工具类
 * 
 * 功能：
 * 1. 图片质量压缩
 * 2. 图片尺寸压缩
 * 3. 图片格式转换
 * 4. 生成缩略图
 * 
 * @author 小跃
 * @date 2026-02-13
 */
@Slf4j
public class ImageCompressUtil {
    
    /**
     * 默认压缩质量（0.0 - 1.0）
     */
    private static final double DEFAULT_QUALITY = 0.8;
    
    /**
     * 默认最大宽度（像素）
     */
    private static final int DEFAULT_MAX_WIDTH = 1920;
    
    /**
     * 默认最大高度（像素）
     */
    private static final int DEFAULT_MAX_HEIGHT = 1080;
    
    /**
     * 缩略图默认宽度
     */
    private static final int THUMBNAIL_WIDTH = 200;
    
    /**
     * 缩略图默认高度
     */
    private static final int THUMBNAIL_HEIGHT = 200;
    
    /**
     * 压缩图片（质量压缩）
     * 
     * @param sourceFile 源文件
     * @param targetFile 目标文件
     * @param quality 压缩质量（0.0-1.0）
     * @throws IOException IO异常
     */
    public static void compressByQuality(File sourceFile, File targetFile, double quality) throws IOException {
        log.debug("开始压缩图片（质量压缩）: source={}, target={}, quality={}", 
                sourceFile.getPath(), targetFile.getPath(), quality);
        
        long startTime = System.currentTimeMillis();
        
        Thumbnails.of(sourceFile)
                .scale(1.0) // 保持原始尺寸
                .outputQuality(quality) // 设置质量
                .toFile(targetFile);
        
        long endTime = System.currentTimeMillis();
        long sourceSize = sourceFile.length();
        long targetSize = targetFile.length();
        double compressRatio = (1 - (double) targetSize / sourceSize) * 100;
        
        log.info("图片压缩完成: 原始大小={}KB, 压缩后={}KB, 压缩率={}%, 耗时={}ms",
                sourceSize / 1024, targetSize / 1024, String.format("%.2f", compressRatio), (endTime - startTime));
    }
    
    /**
     * 压缩图片（质量压缩，使用默认质量）
     * 
     * @param sourceFile 源文件
     * @param targetFile 目标文件
     * @throws IOException IO异常
     */
    public static void compressByQuality(File sourceFile, File targetFile) throws IOException {
        compressByQuality(sourceFile, targetFile, DEFAULT_QUALITY);
    }
    
    /**
     * 压缩图片（尺寸压缩）
     * 
     * @param sourceFile 源文件
     * @param targetFile 目标文件
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @throws IOException IO异常
     */
    public static void compressBySize(File sourceFile, File targetFile, int maxWidth, int maxHeight) throws IOException {
        log.debug("开始压缩图片（尺寸压缩）: source={}, target={}, maxWidth={}, maxHeight={}", 
                sourceFile.getPath(), targetFile.getPath(), maxWidth, maxHeight);
        
        long startTime = System.currentTimeMillis();
        
        Thumbnails.of(sourceFile)
                .size(maxWidth, maxHeight) // 限制最大尺寸
                .keepAspectRatio(true) // 保持宽高比
                .outputQuality(DEFAULT_QUALITY) // 设置质量
                .toFile(targetFile);
        
        long endTime = System.currentTimeMillis();
        log.info("图片尺寸压缩完成: 耗时={}ms", (endTime - startTime));
    }
    
    /**
     * 压缩图片（尺寸压缩，使用默认尺寸）
     * 
     * @param sourceFile 源文件
     * @param targetFile 目标文件
     * @throws IOException IO异常
     */
    public static void compressBySize(File sourceFile, File targetFile) throws IOException {
        compressBySize(sourceFile, targetFile, DEFAULT_MAX_WIDTH, DEFAULT_MAX_HEIGHT);
    }
    
    /**
     * 智能压缩图片（同时压缩质量和尺寸）
     * 
     * @param sourceFile 源文件
     * @param targetFile 目标文件
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @param quality 压缩质量
     * @throws IOException IO异常
     */
    public static void smartCompress(File sourceFile, File targetFile, int maxWidth, int maxHeight, double quality) throws IOException {
        log.debug("开始智能压缩图片: source={}, target={}, maxWidth={}, maxHeight={}, quality={}", 
                sourceFile.getPath(), targetFile.getPath(), maxWidth, maxHeight, quality);
        
        long startTime = System.currentTimeMillis();
        long sourceSize = sourceFile.length();
        
        Thumbnails.of(sourceFile)
                .size(maxWidth, maxHeight)
                .keepAspectRatio(true)
                .outputQuality(quality)
                .toFile(targetFile);
        
        long endTime = System.currentTimeMillis();
        long targetSize = targetFile.length();
        double compressRatio = (1 - (double) targetSize / sourceSize) * 100;
        
        log.info("智能压缩完成: 原始大小={}KB, 压缩后={}KB, 压缩率={}%, 耗时={}ms",
                sourceSize / 1024, targetSize / 1024, String.format("%.2f", compressRatio), (endTime - startTime));
    }
    
    /**
     * 智能压缩图片（使用默认参数）
     * 
     * @param sourceFile 源文件
     * @param targetFile 目标文件
     * @throws IOException IO异常
     */
    public static void smartCompress(File sourceFile, File targetFile) throws IOException {
        smartCompress(sourceFile, targetFile, DEFAULT_MAX_WIDTH, DEFAULT_MAX_HEIGHT, DEFAULT_QUALITY);
    }
    
    /**
     * 生成缩略图
     * 
     * @param sourceFile 源文件
     * @param targetFile 目标文件
     * @param width 缩略图宽度
     * @param height 缩略图高度
     * @throws IOException IO异常
     */
    public static void generateThumbnail(File sourceFile, File targetFile, int width, int height) throws IOException {
        log.debug("开始生成缩略图: source={}, target={}, width={}, height={}", 
                sourceFile.getPath(), targetFile.getPath(), width, height);
        
        long startTime = System.currentTimeMillis();
        
        Thumbnails.of(sourceFile)
                .size(width, height)
                .keepAspectRatio(true)
                .outputQuality(DEFAULT_QUALITY)
                .toFile(targetFile);
        
        long endTime = System.currentTimeMillis();
        log.info("缩略图生成完成: 耗时={}ms", (endTime - startTime));
    }
    
    /**
     * 生成缩略图（使用默认尺寸）
     * 
     * @param sourceFile 源文件
     * @param targetFile 目标文件
     * @throws IOException IO异常
     */
    public static void generateThumbnail(File sourceFile, File targetFile) throws IOException {
        generateThumbnail(sourceFile, targetFile, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
    }
    
    /**
     * 压缩图片（字节数组输入输出）
     * 
     * @param imageBytes 原始图片字节数组
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @param quality 压缩质量
     * @return 压缩后的图片字节数组
     * @throws IOException IO异常
     */
    public static byte[] compressBytes(byte[] imageBytes, int maxWidth, int maxHeight, double quality) throws IOException {
        log.debug("开始压缩图片字节数组: size={}KB, maxWidth={}, maxHeight={}, quality={}", 
                imageBytes.length / 1024, maxWidth, maxHeight, quality);
        
        long startTime = System.currentTimeMillis();
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        Thumbnails.of(inputStream)
                .size(maxWidth, maxHeight)
                .keepAspectRatio(true)
                .outputQuality(quality)
                .toOutputStream(outputStream);
        
        byte[] result = outputStream.toByteArray();
        
        long endTime = System.currentTimeMillis();
        double compressRatio = (1 - (double) result.length / imageBytes.length) * 100;
        
        log.info("字节数组压缩完成: 原始大小={}KB, 压缩后={}KB, 压缩率={}%, 耗时={}ms",
                imageBytes.length / 1024, result.length / 1024, String.format("%.2f", compressRatio), (endTime - startTime));
        
        return result;
    }
    
     /**
     * 压缩图片（字节数组，使用默认参数）
     * 
     * @param imageBytes 原始图片字节数组
     * @return 压缩后的图片字节数组
     * @throws IOException IO异常
     */
    public static byte[] compressBytes(byte[] imageBytes) throws IOException {
        return compressBytes(imageBytes, DEFAULT_MAX_WIDTH, DEFAULT_MAX_HEIGHT, DEFAULT_QUALITY);
    }
    
    /**
     * 转换图片格式
     * 
     * @param sourceFile 源文件
     * @param targetFile 目标文件
     * @param format 目标格式（如：jpg、png、webp）
     * @throws IOException IO异常
     */
    public static void convertFormat(File sourceFile, File targetFile, String format) throws IOException {
        log.debug("开始转换图片格式: source={}, target={}, format={}", 
                sourceFile.getPath(), targetFile.getPath(), format);
        
        long startTime = System.currentTimeMillis();
        
        Thumbnails.of(sourceFile)
                .scale(1.0)
                .outputFormat(format)
                .outputQuality(DEFAULT_QUALITY)
                .toFile(targetFile);
        
        long endTime = System.currentTimeMillis();
        log.info("图片格式转换完成: 耗时={}ms", (endTime - startTime));
    }
    
    /**
     * 按文件大小压缩图片
     * 
     * 说明：自动调整压缩质量，直到文件大小满足要求
     * 
     * @param sourceFile 源文件
     * @param targetFile 目标文件
     * @param targetSizeKB 目标大小（KB）
     * @param maxRetry 最大重试次数
     * @throws IOException IO异常
     */
    public static void compressByFileSize(File sourceFile, File targetFile, int targetSizeKB, int maxRetry) throws IOException {
        log.debug("开始按文件大小压缩: source={}, target={}, targetSize={}KB", 
                sourceFile.getPath(), targetFile.getPath(), targetSizeKB);
        
        long targetSizeBytes = targetSizeKB * 1024L;
        double quality = DEFAULT_QUALITY;
        int retry = 0;
        
        while (retry < maxRetry) {
            // 压缩图片
            Thumbnails.of(sourceFile)
                    .scale(1.0)
                    .outputQuality(quality)
                    .toFile(targetFile);
            
            long currentSize = targetFile.length();
            
            if (currentSize <= targetSizeBytes) {
                log.info("文件大小压缩成功: 目标={}KB, 实际={}KB, 质量={}, 重试次数={}", 
                        targetSizeKB, currentSize / 1024, quality, retry);
                return;
            }
            
            // 调整质量
            quality -= 0.1;
            if (quality < 0.1) {
                quality = 0.1;
            }
            
            retry++;
        }
        
        log.warn("文件大小压缩未达到目标: 目标={}KB, 实际={}KB, 已重试{}次", 
                targetSizeKB, targetFile.length() / 1024, maxRetry);
    }
    
    /**
     * 按文件大小压缩图片（默认重试10次）
     * 
     * @param sourceFile 源文件
     * @param targetFile 目标文件
     * @param targetSizeKB 目标大小（KB）
     * @throws IOException IO异常
     */
    public static void compressByFileSize(File sourceFile, File targetFile, int targetSizeKB) throws IOException {
        compressByFileSize(sourceFile, targetFile, targetSizeKB, 10);
    }
}
