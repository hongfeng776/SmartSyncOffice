package com.smartsync.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartsync.api.dto.FileQueryDTO;
import com.smartsync.api.result.Result;
import com.smartsync.auth.entity.SysFile;
import com.smartsync.auth.entity.SysFileUploadLog;
import com.smartsync.auth.service.FileService;
import com.smartsync.auth.service.FileUploadLogService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private FileUploadLogService fileUploadLogService;

    private static final long LARGE_FILE_THRESHOLD = 10 * 1024 * 1024;

    @PostMapping("/upload")
    public Result<SysFile> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folderId", required = false) Long folderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        try {
            Long uploaderId = 1L;
            String uploaderName = "admin";
            if (userDetails != null) {
                uploaderName = userDetails.getUsername();
            }

            SysFile sysFile = fileService.uploadFile(file, uploaderId, uploaderName, folderId);
            return Result.success(sysFile);
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage());
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/upload/async")
    public Result<Map<String, Object>> uploadFileAsync(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folderId", required = false) Long folderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        try {
            Long uploaderId = 1L;
            String uploaderName = "admin";
            if (userDetails != null) {
                uploaderName = userDetails.getUsername();
            }

            if (file.getSize() > LARGE_FILE_THRESHOLD) {
                CompletableFuture<SysFile> future = fileService.uploadLargeFileAsync(file, uploaderId, uploaderName, folderId);
                Map<String, Object> result = new HashMap<>();
                result.put("message", "大文件正在后台处理，请稍后查看");
                result.put("async", true);
                return Result.success(result);
            } else {
                SysFile sysFile = fileService.uploadFile(file, uploaderId, uploaderName, folderId);
                Map<String, Object> result = new HashMap<>();
                result.put("file", sysFile);
                result.put("async", false);
                return Result.success(result);
            }
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage());
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/download/{fileId}")
    public void downloadFile(@PathVariable Long fileId, HttpServletResponse response) {
        try {
            SysFile sysFile = fileService.getFileById(fileId);
            if (sysFile == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            InputStream inputStream = fileService.downloadFile(fileId);
            String fileName = URLEncoder.encode(sysFile.getOriginalFilename(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

            try (OutputStream outputStream = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
            }
        } catch (IOException e) {
            log.error("文件下载失败: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{fileId}")
    public Result<Void> deleteFile(@PathVariable Long fileId) {
        try {
            fileService.deleteFile(fileId);
            return Result.success();
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage());
            return Result.error("文件删除失败: " + e.getMessage());
        }
    }

    @PostMapping("/list")
    public Result<Page<SysFile>> getFileList(@RequestBody FileQueryDTO queryDTO) {
        Page<SysFile> page = fileService.getFileList(queryDTO);
        return Result.success(page);
    }

    @GetMapping("/{fileId}")
    public Result<SysFile> getFileById(@PathVariable Long fileId) {
        SysFile sysFile = fileService.getFileById(fileId);
        if (sysFile == null) {
            return Result.error("文件不存在");
        }
        return Result.success(sysFile);
    }

    @GetMapping("/preview/{fileId}")
    public Result<String> getFilePreviewUrl(@PathVariable Long fileId) {
        try {
            String url = fileService.getFilePreviewUrl(fileId);
            return Result.success(url);
        } catch (Exception e) {
            log.error("获取预览地址失败: {}", e.getMessage());
            return Result.error("获取预览地址失败");
        }
    }

    @GetMapping("/stream/{fileId}")
    public void streamFile(@PathVariable Long fileId, HttpServletResponse response) {
        try {
            SysFile sysFile = fileService.getFileById(fileId);
            if (sysFile == null || sysFile.getIsDeleted() == 1) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            InputStream inputStream = fileService.downloadFile(fileId);
            
            String contentType = sysFile.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/octet-stream";
            }
            
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "inline; filename=\"" + 
                java.net.URLEncoder.encode(sysFile.getOriginalFilename(), "UTF-8") + "\"");
            response.setHeader("Cache-Control", "max-age=86400");
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

            try (java.io.OutputStream outputStream = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }
            inputStream.close();
        } catch (Exception e) {
            log.error("文件流输出失败: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/log/list")
    public Result<Page<SysFileUploadLog>> getUploadLogList(@RequestBody FileQueryDTO queryDTO) {
        try {
            Page<SysFileUploadLog> page = fileUploadLogService.getUploadLogList(queryDTO);
            return Result.success(page);
        } catch (Exception e) {
            log.error("获取上传日志失败: {}", e.getMessage());
            return Result.error("获取上传日志失败");
        }
    }
}
