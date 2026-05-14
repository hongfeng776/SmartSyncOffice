package com.smartsync.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartsync.api.dto.ChunkUploadDTO;
import com.smartsync.auth.config.MinIOConfig;
import com.smartsync.auth.entity.SysFile;
import com.smartsync.auth.entity.SysFileUpload;
import com.smartsync.auth.mapper.SysFileMapper;
import com.smartsync.auth.mapper.SysFileUploadMapper;
import com.smartsync.auth.utils.FileUploadValidator;
import com.smartsync.auth.utils.MinIOUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class ChunkUploadService {

    @Autowired
    private SysFileUploadMapper fileUploadMapper;

    @Autowired
    private SysFileMapper sysFileMapper;

    @Autowired
    private MinIOUtil minIOUtil;

    @Autowired
    private MinIOConfig minIOConfig;

    @Autowired
    private FileUploadValidator fileUploadValidator;

    private static final String CHUNK_TEMP_DIR = System.getProperty("java.io.tmpdir") + "/file-chunks/";

    public Object checkChunk(String uploadId, String filename, Long totalSize, Long folderId) {
        LambdaQueryWrapper<SysFileUpload> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFileUpload::getUploadId, uploadId);
        SysFileUpload upload = fileUploadMapper.selectOne(wrapper);

        if (upload != null) {
            String[] chunks = upload.getUploadedChunkNumbers().split(",");
            Set<Integer> uploadedChunks = new HashSet<>();
            for (String chunk : chunks) {
                if (!chunk.isEmpty()) {
                    uploadedChunks.add(Integer.parseInt(chunk));
                }
            }
            
            var result = new java.util.HashMap<String, Object>();
            result.put("uploaded", uploadedChunks);
            result.put("uploadId", uploadId);
            result.put("totalChunks", upload.getTotalChunks());
            return result;
        }

        String newUploadId = UUID.randomUUID().toString().replace("-", "");
        var result = new java.util.HashMap<String, Object>();
        result.put("uploaded", new HashSet<>());
        result.put("uploadId", newUploadId);
        return result;
    }

    @Transactional
    public Object uploadChunk(ChunkUploadDTO dto) {
        String uploadId = dto.getUploadId();
        MultipartFile chunkFile = dto.getFile();

        try {
            LambdaQueryWrapper<SysFileUpload> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysFileUpload::getUploadId, uploadId);
            SysFileUpload upload = fileUploadMapper.selectOne(wrapper);

            if (upload == null) {
                upload = new SysFileUpload();
                upload.setUploadId(uploadId);
                upload.setFilename(dto.getFilename());
                upload.setTotalSize(dto.getTotalSize());
                upload.setTotalChunks(dto.getTotalChunks());
                upload.setUploadedChunks(0);
                upload.setUploadedChunkNumbers("");
                upload.setFolderId(dto.getFolderId());
                upload.setUploaderId(dto.getUploaderId() != null ? String.valueOf(dto.getUploaderId()) : "1");
                upload.setUploaderName(dto.getUploaderName() != null ? dto.getUploaderName() : "admin");
                upload.setStatus("UPLOADING");
                upload.setCreateTime(LocalDateTime.now());
                upload.setUpdateTime(LocalDateTime.now());
                fileUploadMapper.insert(upload);
            }

            if (upload.getUploadedChunkNumbers().contains("," + dto.getChunkNumber() + ",") ||
                upload.getUploadedChunkNumbers().startsWith(dto.getChunkNumber() + ",") ||
                upload.getUploadedChunkNumbers().endsWith("," + dto.getChunkNumber())) {
                var result = new java.util.HashMap<String, Object>();
                result.put("success", true);
                result.put("uploadId", uploadId);
                result.put("chunkNumber", dto.getChunkNumber());
                result.put("skip", true);
                return result;
            }

            Path chunkDir = Paths.get(CHUNK_TEMP_DIR, uploadId);
            if (!Files.exists(chunkDir)) {
                Files.createDirectories(chunkDir);
            }

            Path chunkPath = chunkDir.resolve(String.valueOf(dto.getChunkNumber()));
            try (InputStream is = chunkFile.getInputStream();
                 OutputStream os = Files.newOutputStream(chunkPath)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }

            Set<Integer> uploadedChunks = new HashSet<>();
            if (!upload.getUploadedChunkNumbers().isEmpty()) {
                String[] chunks = upload.getUploadedChunkNumbers().split(",");
                for (String chunk : chunks) {
                    if (!chunk.isEmpty()) {
                        uploadedChunks.add(Integer.parseInt(chunk));
                    }
                }
            }
            uploadedChunks.add(dto.getChunkNumber());
            upload.setUploadedChunkNumbers(String.join(",", 
                uploadedChunks.stream().map(String::valueOf).toArray(String[]::new)));
            upload.setUploadedChunks(uploadedChunks.size());
            upload.setUpdateTime(LocalDateTime.now());
            fileUploadMapper.updateById(upload);

            var result = new java.util.HashMap<String, Object>();
            result.put("success", true);
            result.put("uploadId", uploadId);
            result.put("chunkNumber", dto.getChunkNumber());
            result.put("uploadedChunks", uploadedChunks.size());
            result.put("totalChunks", dto.getTotalChunks());
            result.put("completed", uploadedChunks.size() == dto.getTotalChunks());

            return result;
        } catch (Exception e) {
            log.error("分片上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("分片上传失败: " + e.getMessage());
        }
    }

    @Transactional
    public Object mergeChunks(String uploadId) {
        try {
            LambdaQueryWrapper<SysFileUpload> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysFileUpload::getUploadId, uploadId);
            SysFileUpload upload = fileUploadMapper.selectOne(wrapper);

            if (upload == null) {
                throw new RuntimeException("上传记录不存在");
            }

            Path chunkDir = Paths.get(CHUNK_TEMP_DIR, uploadId);
            if (!Files.exists(chunkDir)) {
                throw new RuntimeException("分片目录不存在");
            }

            Path mergedFile = chunkDir.resolve("merged_" + upload.getFilename());
            try (OutputStream os = Files.newOutputStream(mergedFile)) {
                for (int i = 1; i <= upload.getTotalChunks(); i++) {
                    Path chunkPath = chunkDir.resolve(String.valueOf(i));
                    if (Files.exists(chunkPath)) {
                        try (InputStream is = Files.newInputStream(chunkPath)) {
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = is.read(buffer)) != -1) {
                                os.write(buffer, 0, bytesRead);
                            }
                        }
                    }
                }
            }

            String fileExtension = getFileExtension(upload.getFilename());
            String storedPath = generateStoredPath(upload.getFilename());
            String bucketName = minIOConfig.getBucketName();
            
            minIOUtil.createBucket(bucketName);

            File mergedFileObj = mergedFile.toFile();
            InputStream mergedInputStream = new FileInputStream(mergedFileObj);
            long fileSize = mergedFileObj.length();
            
            minIOUtil.minioClient.putObject(
                io.minio.PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(storedPath)
                    .stream(mergedInputStream, fileSize, -1)
                    .contentType(getContentType(fileExtension))
                    .build()
            );
            
            mergedInputStream.close();

            String fileType = getFileType(fileExtension);
            SysFile sysFile = new SysFile();
            sysFile.setOriginalFilename(upload.getFilename());
            sysFile.setStoredFilename(storedPath.substring(storedPath.lastIndexOf("/") + 1));
            sysFile.setFilePath(storedPath);
            sysFile.setFileSize(formatFileSize(fileSize));
            sysFile.setFileType(fileType);
            sysFile.setContentType(getContentType(fileExtension));
            sysFile.setFileExtension(fileExtension);
            sysFile.setBucketName(bucketName);
            sysFile.setFolderId(upload.getFolderId());
            sysFile.setUploaderId(upload.getUploaderId());
            sysFile.setUploaderName(upload.getUploaderName());
            sysFile.setDownloadCount(0);
            sysFile.setIsDeleted(0);
            sysFile.setCreateTime(LocalDateTime.now());
            sysFile.setUpdateTime(LocalDateTime.now());

            sysFileMapper.insert(sysFile);

            upload.setStatus("COMPLETED");
            upload.setStoredPath(storedPath);
            upload.setUpdateTime(LocalDateTime.now());
            fileUploadMapper.updateById(upload);

            try {
                Files.walk(chunkDir)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.warn("删除临时文件失败: {}", path);
                        }
                    });
                Files.deleteIfExists(chunkDir);
            } catch (Exception e) {
                log.warn("清理临时文件目录失败: {}", e.getMessage());
            }

            var result = new java.util.HashMap<String, Object>();
            result.put("success", true);
            result.put("fileId", sysFile.getId());
            result.put("filename", sysFile.getOriginalFilename());
            result.put("fileSize", sysFile.getFileSize());
            result.put("fileType", sysFile.getFileType());
            return result;

        } catch (Exception e) {
            log.error("合并分片失败: {}", e.getMessage(), e);
            throw new RuntimeException("合并分片失败: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private String generateStoredPath(String filename) {
        String extension = getFileExtension(filename);
        String dateStr = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return dateStr + "/" + uuid + "." + extension;
    }

    private String getContentType(String extension) {
        switch (extension) {
            case "pdf": return "application/pdf";
            case "jpg": case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "gif": return "image/gif";
            case "bmp": return "image/bmp";
            case "webp": return "image/webp";
            case "svg": return "image/svg+xml";
            case "mp4": return "video/mp4";
            case "avi": return "video/x-msvideo";
            case "mov": return "video/quicktime";
            case "mp3": return "audio/mpeg";
            case "wav": return "audio/wav";
            case "doc": return "application/msword";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls": return "application/vnd.ms-excel";
            case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt": return "application/vnd.ms-powerpoint";
            case "pptx": return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt": return "text/plain";
            default: return "application/octet-stream";
        }
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

    private String formatFileSize(long size) {
        if (size <= 0) {
            return "0 B";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new java.text.DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
