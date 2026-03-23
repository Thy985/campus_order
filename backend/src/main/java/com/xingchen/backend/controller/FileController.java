package com.xingchen.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.xingchen.backend.config.FileConfig;
import com.xingchen.backend.entity.File;
import com.xingchen.backend.service.FileService;
import com.xingchen.backend.util.Result;
import com.xingchen.backend.util.file.FileValidateUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
@Tag(name = "File Management", description = "File upload, download, delete endpoints")
public class FileController {

    private final FileService fileService;
    private final FileConfig fileConfig;

    @SaCheckLogin
    @PostMapping("/upload")
    @Operation(summary = "Upload file", description = "Support image, document upload")
    public Result<Map<String, Object>> uploadFile(
            @Parameter(description = "File") @RequestParam("file") MultipartFile file,
            @Parameter(description = "File type: 1-avatar, 2-product image, 3-merchant image")
            @RequestParam(value = "fileType", defaultValue = "1") Integer fileType) {

        Long userId = StpUtil.getLoginIdAsLong();

        log.info("File upload request: fileName={}, size={}, fileType={}, userId={}",
                file.getOriginalFilename(), FileValidateUtil.formatFileSize(file.getSize()),
                fileType, userId);

        String fileUrl = fileService.uploadFile(file, fileType, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("fileUrl", fileUrl);
        result.put("fileName", file.getOriginalFilename());
        result.put("fileSize", file.getSize());
        result.put("fileSizeFormatted", FileValidateUtil.formatFileSize(file.getSize()));
        result.put("fileType", fileType);

        return Result.success("File uploaded successfully", result);
    }

    @SaCheckLogin
    @PostMapping("/upload/batch")
    @Operation(summary = "Batch upload files", description = "Support multiple file upload")
    public Result<Map<String, Object>> uploadFiles(
            @Parameter(description = "File list") @RequestParam("files") MultipartFile[] files,
            @Parameter(description = "File type") @RequestParam(value = "fileType", defaultValue = "1") Integer fileType) {

        Long userId = StpUtil.getLoginIdAsLong();

        log.info("Batch file upload request: fileCount={}, fileType={}, userId={}",
                files.length, fileType, userId);

        Map<String, Object> result = new HashMap<>();
        java.util.List<Map<String, Object>> uploadedFiles = new java.util.ArrayList<>();

        for (MultipartFile file : files) {
            try {
                String fileUrl = fileService.uploadFile(file, fileType, userId);

                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("fileUrl", fileUrl);
                fileInfo.put("fileName", file.getOriginalFilename());
                fileInfo.put("fileSize", file.getSize());
                fileInfo.put("success", true);

                uploadedFiles.add(fileInfo);
            } catch (Exception e) {
                log.error("File upload failed: fileName={}", file.getOriginalFilename(), e);

                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("fileName", file.getOriginalFilename());
                fileInfo.put("success", false);
                fileInfo.put("error", e.getMessage());

                uploadedFiles.add(fileInfo);
            }
        }

        result.put("total", files.length);
        result.put("success", uploadedFiles.stream().filter(f -> (Boolean) f.get("success")).count());
        result.put("failed", uploadedFiles.stream().filter(f -> !(Boolean) f.get("success")).count());
        result.put("files", uploadedFiles);

        return Result.success("Batch upload completed", result);
    }

    @GetMapping("/download/{fileId}")
    @Operation(summary = "Download file", description = "Download file by ID")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "File ID") @PathVariable Long fileId) {

        try {
            File file = fileService.getFileInfo(fileId);

            String fullPath = fileConfig.getUploadDir() + java.io.File.separator + file.getFilePath();
            Path path = Paths.get(fullPath);

            if (!Files.exists(path)) {
                log.error("File does not exist: path={}", fullPath);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(path.toFile());

            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + file.getFileName() + "\"")
                    .body(resource);

        } catch (IOException e) {
            log.error("File download failed: fileId={}", fileId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/view/**")
    @Operation(summary = "View file", description = "Online file preview (image, PDF)")
    public ResponseEntity<Resource> viewFile() {
        try {
            String requestPath = org.springframework.web.servlet.HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE;
            String filePath = ((String) org.springframework.web.context.request.RequestContextHolder
                    .currentRequestAttributes()
                    .getAttribute(requestPath, org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST))
                    .replace("/api/file/view/", "");

            String fullPath = fileConfig.getUploadDir() + java.io.File.separator + filePath;
            Path path = Paths.get(fullPath);

            if (!Files.exists(path)) {
                log.error("File does not exist: path={}", fullPath);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(path.toFile());

            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=31536000")
                    .body(resource);

        } catch (IOException e) {
            log.error("File access failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @SaCheckLogin
    @DeleteMapping("/{fileId}")
    @Operation(summary = "Delete file", description = "Delete specified file")
    public Result<Void> deleteFile(
            @Parameter(description = "File ID") @PathVariable Long fileId) {

        Long userId = StpUtil.getLoginIdAsLong();
        fileService.deleteFile(fileId, userId);

        return Result.success("File deleted successfully", null);
    }

    @SaCheckLogin
    @GetMapping("/{fileId}")
    @Operation(summary = "Get file info", description = "Get file details")
    public Result<File> getFileInfo(
            @Parameter(description = "File ID") @PathVariable Long fileId) {

        File file = fileService.getFileInfo(fileId);
        return Result.success(file);
    }

    @GetMapping("/config")
    @Operation(summary = "Get upload config", description = "Get file upload configuration")
    public Result<Map<String, Object>> getUploadConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("maxSize", fileConfig.getMaxSize());
        config.put("maxSizeFormatted", FileValidateUtil.formatFileSize(fileConfig.getMaxSize()));
        config.put("allowedExtensions", fileConfig.getAllowedExtensions());
        config.put("uploadDir", fileConfig.getUploadDir());
        config.put("accessUrl", fileConfig.getAccessUrl());

        return Result.success(config);
    }
}
