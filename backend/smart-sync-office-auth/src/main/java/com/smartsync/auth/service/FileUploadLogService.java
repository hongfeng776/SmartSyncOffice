package com.smartsync.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartsync.api.dto.FileQueryDTO;
import com.smartsync.auth.entity.SysFile;
import com.smartsync.auth.entity.SysFileUploadLog;
import com.smartsync.auth.mapper.SysFileUploadLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@Service
public class FileUploadLogService {

    @Autowired
    private SysFileUploadLogMapper uploadLogMapper;

    public SysFileUploadLog createUploadLog(String originalFilename, Long fileSize, 
                                              String contentType, Long folderId, 
                                              Long uploaderId, String uploaderName) {
        SysFileUploadLog uploadLog = new SysFileUploadLog();
        uploadLog.setOriginalFilename(originalFilename);
        uploadLog.setFileSize(fileSize);
        uploadLog.setFileExtension(getFileExtension(originalFilename));
        uploadLog.setContentType(contentType);
        uploadLog.setFolderId(folderId);
        uploadLog.setUploaderId(String.valueOf(uploaderId));
        uploadLog.setUploaderName(uploaderName);
        uploadLog.setUploadStatus("UPLOADING");
        uploadLog.setUploaderIp(getClientIp());
        uploadLog.setUserAgent(getUserAgent());
        uploadLog.setCreateTime(LocalDateTime.now());
        uploadLog.setUpdateTime(LocalDateTime.now());
        
        uploadLogMapper.insert(uploadLog);
        return uploadLog;
    }

    public void updateUploadSuccess(SysFileUploadLog uploadLog, SysFile file) {
        uploadLog.setUploadStatus("SUCCESS");
        uploadLog.setStoredFilename(file.getStoredFilename());
        uploadLog.setFilePath(file.getFilePath());
        uploadLog.setBucketName(file.getBucketName());
        uploadLog.setFileId(file.getId());
        uploadLog.setUpdateTime(LocalDateTime.now());
        
        uploadLogMapper.updateById(uploadLog);
        log.info("文件上传成功记录已更新: {}", file.getOriginalFilename());
    }

    public void updateUploadFailed(SysFileUploadLog uploadLog, String errorMessage) {
        uploadLog.setUploadStatus("FAILED");
        uploadLog.setErrorMessage(errorMessage);
        uploadLog.setUpdateTime(LocalDateTime.now());
        
        uploadLogMapper.updateById(uploadLog);
        log.warn("文件上传失败记录已更新: {} - {}", uploadLog.getOriginalFilename(), errorMessage);
    }

    public Page<SysFileUploadLog> getUploadLogList(FileQueryDTO queryDTO) {
        Page<SysFileUploadLog> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<SysFileUploadLog> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            wrapper.like(SysFileUploadLog::getOriginalFilename, queryDTO.getKeyword());
        }
        if (queryDTO.getUploaderId() != null && !queryDTO.getUploaderId().isEmpty()) {
            wrapper.eq(SysFileUploadLog::getUploaderId, queryDTO.getUploaderId());
        }
        
        wrapper.orderByDesc(SysFileUploadLog::getCreateTime);
        return uploadLogMapper.selectPage(page, wrapper);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return "unknown";
            }
            HttpServletRequest request = attributes.getRequest();
            
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            
            return ip;
        } catch (Exception e) {
            log.error("获取客户端IP失败", e);
            return "unknown";
        }
    }

    private String getUserAgent() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return "";
            }
            HttpServletRequest request = attributes.getRequest();
            String userAgent = request.getHeader("User-Agent");
            return userAgent != null ? userAgent.substring(0, Math.min(userAgent.length(), 500)) : "";
        } catch (Exception e) {
            log.error("获取UserAgent失败", e);
            return "";
        }
    }
}
