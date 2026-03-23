package com.xingchen.backend.util.file;

import com.xingchen.backend.config.FileConfig;
import com.xingchen.backend.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 文件验证工具类测试
 *
 * @author 小跃
 * @date 2026-03-14
 */
class FileValidateUtilTest {

    private FileConfig fileConfig;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileConfig = new FileConfig();
        fileConfig.setMaxSize(10 * 1024 * 1024); // 10MB
        fileConfig.setAllowedExtensions("jpg,jpeg,png,gif");
    }

    // ==================== validateFilename 测试 ====================

    @ParameterizedTest
    @ValueSource(strings = {
        "test.jpg",
        "file.png",
        "image.jpeg",
        "photo.gif",
        "my-file.jpg",
        "file_123.png"
    })
    @DisplayName("验证正常文件名")
    void testValidateFilename_Valid(String filename) {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                filename,
                "image/jpeg",
                "test content".getBytes()
        );

        assertDoesNotThrow(() -> FileValidateUtil.validateFile(file, fileConfig));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("验证空文件名")
    void testValidateFilename_Empty(String filename) {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                filename,
                "image/jpeg",
                "test content".getBytes()
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            FileValidateUtil.validateFile(file, fileConfig);
        });

        assertTrue(exception.getMessage().contains("文件名不能为空"));
    }

    @Test
    @DisplayName("验证过长文件名")
    void testValidateFilename_TooLong() {
        String longName = "a".repeat(256) + ".jpg";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                longName,
                "image/jpeg",
                "test content".getBytes()
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            FileValidateUtil.validateFile(file, fileConfig);
        });

        assertTrue(exception.getMessage().contains("文件名过长"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "test:image.jpg",
        "test/image.jpg",
        "test*image.jpg",
        "test?image.jpg",
        "test<image.jpg",
        "test>image.jpg",
        "test|image.jpg",
        "test\\image.jpg"
    })
    @DisplayName("验证包含非法字符的文件名")
    void testValidateFilename_IllegalCharacters(String filename) {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                filename,
                "image/jpeg",
                "test content".getBytes()
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            FileValidateUtil.validateFile(file, fileConfig);
        });

        assertTrue(exception.getMessage().contains("非法字符"));
    }

    // ==================== validateFileType 测试 ====================

    @ParameterizedTest
    @CsvSource({
        "test.jpg, image/jpeg",
        "test.jpeg, image/jpeg",
        "test.png, image/png",
        "test.gif, image/gif"
    })
    @DisplayName("验证支持的文件类型")
    void testValidateFileType_Supported(String filename, String contentType) {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                filename,
                contentType,
                "test content".getBytes()
        );

        assertDoesNotThrow(() -> FileValidateUtil.validateFile(file, fileConfig));
    }

    @ParameterizedTest
    @CsvSource({
        "test.bmp, image/bmp",
        "test.pdf, application/pdf",
        "test.exe, application/octet-stream",
        "test.txt, text/plain"
    })
    @DisplayName("验证不支持的文件类型")
    void testValidateFileType_Unsupported(String filename, String contentType) {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                filename,
                contentType,
                "test content".getBytes()
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            FileValidateUtil.validateFile(file, fileConfig);
        });

        assertTrue(exception.getMessage().contains("不支持的文件类型") ||
                   exception.getMessage().contains("不允许上传"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"exe", "bat", "cmd", "sh", "ps1", "vbs", "js", "jar", "war"})
    @DisplayName("验证危险文件类型")
    void testValidateFileType_Dangerous(String extension) {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test." + extension,
                "application/octet-stream",
                "test content".getBytes()
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            FileValidateUtil.validateFile(file, fileConfig);
        });

        assertTrue(exception.getMessage().contains("不允许上传"));
    }

    // ==================== validateFileSize 测试 ====================

    @ParameterizedTest
    @CsvSource({
        "1024, 10485760",      // 1KB < 10MB
        "1048576, 10485760",   // 1MB < 10MB
        "5242880, 10485760",   // 5MB < 10MB
        "10485760, 10485760"   // 10MB = 10MB
    })
    @DisplayName("验证正常文件大小")
    void testValidateFileSize_Valid(int fileSize, int maxSize) {
        fileConfig.setMaxSize(maxSize);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[fileSize]
        );

        assertDoesNotThrow(() -> FileValidateUtil.validateFile(file, fileConfig));
    }

    @Test
    @DisplayName("验证超过限制的文件大小")
    void testValidateFileSize_ExceedLimit() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[11 * 1024 * 1024] // 11MB
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            FileValidateUtil.validateFile(file, fileConfig);
        });

        assertTrue(exception.getMessage().contains("文件大小超过限制"));
    }

    @Test
    @DisplayName("验证空文件大小")
    void testValidateFileSize_Empty() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[0]
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            FileValidateUtil.validateFile(file, fileConfig);
        });

        assertTrue(exception.getMessage().contains("文件大小不能为空") ||
                   exception.getMessage().contains("文件不能为空"));
    }

    // ==================== validateImage 测试 ====================

    @ParameterizedTest
    @ValueSource(strings = {"jpg", "jpeg", "png", "gif", "bmp", "webp"})
    @DisplayName("验证有效的图片类型")
    void testValidateImage_Valid(String extension) {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test." + extension,
                "image/" + extension,
                "test".getBytes()
        );

        assertTrue(FileValidateUtil.isImage(file));
    }

    @ParameterizedTest
    @ValueSource(strings = {"pdf", "doc", "txt", "mp4", "exe"})
    @DisplayName("验证无效的图片类型")
    void testValidateImage_Invalid(String extension) {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test." + extension,
                "application/octet-stream",
                "test".getBytes()
        );

        assertFalse(FileValidateUtil.isImage(file));
    }

    // ==================== 其他方法测试 ====================

    @Test
    @DisplayName("验证空文件")
    void testValidateFile_NullFile() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            FileValidateUtil.validateFile(null, fileConfig);
        });

        assertTrue(exception.getMessage().contains("文件不能为空"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt"})
    @DisplayName("验证文档类型")
    void testIsDocument(String extension) {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test." + extension,
                "application/octet-stream",
                "test".getBytes()
        );

        assertTrue(FileValidateUtil.isDocument(file));
    }

    @ParameterizedTest
    @ValueSource(strings = {"mp4", "avi", "mov", "wmv", "flv", "mkv"})
    @DisplayName("验证视频类型")
    void testIsVideo(String extension) {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test." + extension,
                "video/mp4",
                "test".getBytes()
        );

        assertTrue(FileValidateUtil.isVideo(file));
    }

    @ParameterizedTest
    @CsvSource({
        "test.jpg, jpg",
        "test.jpeg, jpeg",
        "test.PNG, png",
        "test.GIF, gif"
    })
    @DisplayName("验证获取文件扩展名")
    void testGetExtension(String filename, String expectedExtension) {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                filename,
                "image/jpeg",
                "test".getBytes()
        );

        assertEquals(expectedExtension, FileValidateUtil.getExtension(file));
    }

    @ParameterizedTest
    @CsvSource({
        "512, 512B",
        "1024, 1.00KB",
        "1048576, 1.00MB",
        "1073741824, 1.00GB"
    })
    @DisplayName("验证文件大小格式化")
    void testFormatFileSize(long size, String expected) {
        assertEquals(expected, FileValidateUtil.formatFileSize(size));
    }
}
