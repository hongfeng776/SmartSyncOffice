package com.smartsync.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartsync.api.dto.FileQueryDTO;
import com.smartsync.auth.config.MinIOConfig;
import com.smartsync.auth.entity.SysFile;
import com.smartsync.auth.entity.SysFileUploadLog;
import com.smartsync.auth.mapper.SysFileMapper;
import com.smartsync.auth.mapper.SysFileUploadLogMapper;
import com.smartsync.auth.utils.MinIOUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class FileService {

    @Autowired
    private SysFileMapper sysFileMapper;

    @Autowired
    private MinIOUtil minIOUtil;

    @Autowired
    private MinIOConfig minIOConfig;

    @Autowired
    private FileUploadValidator fileUploadValidator;

    @Autowired
    private FileUploadLogService fileUploadLogService;

    @Autowired
    private SysFileUploadLogMapper uploadLogMapper;

    private static final long LARGE_FILE_THRESHOLD = 10 * 1024 * 1024;

    @Transactional
    public SysFile uploadFile(MultipartFile file, Long uploaderId, String uploaderName, Long folderId) {
        String originalFilename = file.getOriginalFilename();
        log.info("开始上传文件: {}, 大小: {} bytes", originalFilename, file.getSize());

        fileUploadValidator.validateFile(file);

        SysFileUploadLog uploadLog = fileUploadLogService.createUploadLog(
            originalFilename,
            file.getSize(),
            file.getContentType(),
            folderId,
            uploaderId,
            uploaderName
        );

        try {
            String fileExtension = getFileExtension(originalFilename);
            String fileType = getFileType(fileExtension);
            String storedFilename = generateStoredFilename(originalFilename);
            String bucketName = minIOConfig.getBucketName();

            minIOUtil.uploadFile(bucketName, storedFilename, file);

            SysFile sysFile = new SysFile();
            sysFile.setOriginalFilename(originalFilename);
            sysFile.setStoredFilename(storedFilename);
            sysFile.setFilePath(storedFilename);
            sysFile.setFileSize(formatFileSize(file.getSize()));
            sysFile.setFileType(fileType);
            sysFile.setContentType(file.getContentType());
            sysFile.setFileExtension(fileExtension);
            sysFile.setBucketName(bucketName);
            sysFile.setFolderId(folderId);
            sysFile.setUploaderId(String.valueOf(uploaderId));
            sysFile.setUploaderName(uploaderName);
            sysFile.setDownloadCount(0);
            sysFile.setIsDeleted(0);
            sysFile.setCreateTime(LocalDateTime.now());
            sysFile.setUpdateTime(LocalDateTime.now());

            sysFileMapper.insert(sysFile);

            fileUploadLogService.updateUploadSuccess(uploadLog, sysFile);

            log.info("文件上传成功, 文件ID: {}", sysFile.getId());
            return sysFile;
        } catch (Exception e) {
            fileUploadLogService.updateUploadFailed(uploadLog, e.getMessage());
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Transactional
    public SysFile uploadFile(MultipartFile file, Long uploaderId, String uploaderName) {
        return uploadFile(file, uploaderId, uploaderName, null);
    }

    @Async("fileUploadExecutor")
    public CompletableFuture<SysFile> uploadLargeFileAsync(MultipartFile file, Long uploaderId, String uploaderName, Long folderId) {
        log.info("异步上传大文件: {}, 线程: {}", file.getOriginalFilename(), Thread.currentThread().getName());
        
        try {
            fileUploadValidator.validateFile(file);
        } catch (Exception e) {
            log.error("大文件校验失败: {}", e.getMessage());
            SysFileUploadLog uploadLog = fileUploadLogService.createUploadLog(
                file.getOriginalFilename(),
                file.getSize(),
                file.getContentType(),
                folderId,
                uploaderId,
                uploaderName
            );
            fileUploadLogService.updateUploadFailed(uploadLog, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        
        SysFile sysFile = uploadFile(file, uploaderId, uploaderName, folderId);
        return CompletableFuture.completedFuture(sysFile);
    }

    @Async("fileUploadExecutor")
    public CompletableFuture<SysFile> uploadLargeFileAsync(MultipartFile file, Long uploaderId, String uploaderName) {
        return uploadLargeFileAsync(file, uploaderId, uploaderName, null);
    }

    public InputStream downloadFile(Long fileId) {
        SysFile sysFile = sysFileMapper.selectById(fileId);
        if (sysFile == null || sysFile.getIsDeleted() == 1) {
            throw new RuntimeException("文件不存在");
        }

        sysFile.setDownloadCount(sysFile.getDownloadCount() + 1);
        sysFileMapper.updateById(sysFile);

        return minIOUtil.downloadFile(sysFile.getBucketName(), sysFile.getFilePath());
    }

    @Transactional
    public void deleteFile(Long fileId) {
        SysFile sysFile = sysFileMapper.selectById(fileId);
        if (sysFile == null) {
            throw new RuntimeException("文件不存在");
        }

        try {
            minIOUtil.deleteFile(sysFile.getBucketName(), sysFile.getFilePath());
        } catch (Exception e) {
            log.error("删除MinIO文件失败，继续删除数据库记录: {}", e.getMessage());
        }

        sysFile.setIsDeleted(1);
        sysFile.setUpdateTime(LocalDateTime.now());
        sysFileMapper.updateById(sysFile);

        LambdaQueryWrapper<SysFileUploadLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(SysFileUploadLog::getFileId, fileId);
        List<SysFileUploadLog> logs = uploadLogMapper.selectList(logWrapper);
        for (SysFileUploadLog log : logs) {
            log.setUploadStatus("DELETED");
            log.setUpdateTime(LocalDateTime.now());
            uploadLogMapper.updateById(log);
        }

        log.info("文件已删除，文件ID: {}", fileId);
    }

    public Page<SysFile> getFileList(FileQueryDTO queryDTO) {
        Page<SysFile> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFile::getIsDeleted, 0);

        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            wrapper.like(SysFile::getOriginalFilename, queryDTO.getKeyword());
        }
        if (queryDTO.getFileType() != null && !queryDTO.getFileType().isEmpty()) {
            wrapper.eq(SysFile::getFileType, queryDTO.getFileType());
        }
        if (queryDTO.getUploaderId() != null && !queryDTO.getUploaderId().isEmpty()) {
            wrapper.eq(SysFile::getUploaderId, queryDTO.getUploaderId());
        }
        if (queryDTO.getFolderId() != null) {
            wrapper.eq(SysFile::getFolderId, queryDTO.getFolderId());
        } else {
            wrapper.isNull(SysFile::getFolderId);
        }

        wrapper.orderByDesc(SysFile::getCreateTime);
        return sysFileMapper.selectPage(page, wrapper);
    }

    public SysFile getFileById(Long fileId) {
        return sysFileMapper.selectById(fileId);
    }

    public String getFilePreviewUrl(Long fileId) {
        SysFile sysFile = sysFileMapper.selectById(fileId);
        if (sysFile == null || sysFile.getIsDeleted() == 1) {
            throw new RuntimeException("文件不存在");
        }
        return minIOUtil.getFileUrl(sysFile.getBucketName(), sysFile.getFilePath());
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private String getFileType(String extension) {
        String imageExtensions = "jpg,jpeg,png,gif,bmp,webp,svg";
        String documentExtensions = "doc,docx,xls,xlsx,ppt,pptx,pdf,txt";
        String videoExtensions = "mp4,avi,mov,wmv,flv,mkv";
        String audioExtensions = "mp3,wav,flac,aac";

        if (imageExtensions.contains(extension)) {
            return "image";
        } else if (documentExtensions.contains(extension)) {
            return "document";
        } else if (videoExtensions.contains(extension)) {
            return "video";
        } else if (audioExtensions.contains(extension)) {
            return "audio";
        } else {
            return "other";
        }
    }

    private String generateStoredFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return dateStr + "/" + uuid + "." + extension;
    }

    private String formatFileSize(long size) {
        if (size <= 0) {
            return "0 B";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
