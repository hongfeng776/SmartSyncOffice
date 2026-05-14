package com.smartsync.auth.utils;

import com.smartsync.auth.config.MinIOConfig;
import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MinIOUtil {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinIOConfig minIOConfig;

    public MinioClient getMinioClient() {
        return minioClient;
    }

    private static final Map<String, String> CONTENT_TYPE_MAP = new HashMap<>();

    static {
        CONTENT_TYPE_MAP.put("pdf", "application/pdf");
        CONTENT_TYPE_MAP.put("jpg", "image/jpeg");
        CONTENT_TYPE_MAP.put("jpeg", "image/jpeg");
        CONTENT_TYPE_MAP.put("png", "image/png");
        CONTENT_TYPE_MAP.put("gif", "image/gif");
        CONTENT_TYPE_MAP.put("bmp", "image/bmp");
        CONTENT_TYPE_MAP.put("webp", "image/webp");
        CONTENT_TYPE_MAP.put("svg", "image/svg+xml");
        CONTENT_TYPE_MAP.put("mp4", "video/mp4");
        CONTENT_TYPE_MAP.put("avi", "video/x-msvideo");
        CONTENT_TYPE_MAP.put("mov", "video/quicktime");
        CONTENT_TYPE_MAP.put("wmv", "video/x-ms-wmv");
        CONTENT_TYPE_MAP.put("flv", "video/x-flv");
        CONTENT_TYPE_MAP.put("mkv", "video/x-matroska");
        CONTENT_TYPE_MAP.put("mp3", "audio/mpeg");
        CONTENT_TYPE_MAP.put("wav", "audio/wav");
        CONTENT_TYPE_MAP.put("flac", "audio/flac");
        CONTENT_TYPE_MAP.put("aac", "audio/aac");
        CONTENT_TYPE_MAP.put("doc", "application/msword");
        CONTENT_TYPE_MAP.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        CONTENT_TYPE_MAP.put("xls", "application/vnd.ms-excel");
        CONTENT_TYPE_MAP.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        CONTENT_TYPE_MAP.put("ppt", "application/vnd.ms-powerpoint");
        CONTENT_TYPE_MAP.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        CONTENT_TYPE_MAP.put("txt", "text/plain");
    }

    public boolean bucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.error("检查bucket是否存在失败: {}", e.getMessage());
            return false;
        }
    }

    public void createBucket(String bucketName) {
        try {
            if (!bucketExists(bucketName)) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                setBucketPolicy(bucketName);
            }
        } catch (Exception e) {
            log.error("创建bucket失败: {}", e.getMessage());
        }
    }

    private void setBucketPolicy(String bucketName) {
        try {
            String policy = "{\n" +
                "    \"Version\": \"2012-10-17\",\n" +
                "    \"Statement\": [\n" +
                "        {\n" +
                "            \"Effect\": \"Allow\",\n" +
                "            \"Principal\": \"*\",\n" +
                "            \"Action\": [\n" +
                "                \"s3:GetObject\"\n" +
                "            ],\n" +
                "            \"Resource\": [\n" +
                "                \"arn:aws:s3:::" + bucketName + "/*\"\n" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}";
            
            minioClient.setBucketPolicy(
                SetBucketPolicyArgs.builder()
                    .bucket(bucketName)
                    .config(policy)
                    .build()
            );
            log.info("Bucket策略设置成功: {}", bucketName);
        } catch (Exception e) {
            log.error("设置Bucket策略失败: {}", e.getMessage());
        }
    }

    public String uploadFile(String bucketName, String objectName, MultipartFile file) {
        try {
            createBucket(bucketName);
            
            String contentType = getCorrectContentType(file);
            log.info("上传文件: {}, Content-Type: {}", objectName, contentType);
            
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(contentType)
                    .build()
            );
            return objectName;
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage());
            throw new RuntimeException("文件上传失败");
        }
    }

    private String getCorrectContentType(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1)
                .toLowerCase(Locale.ROOT);
            String contentType = CONTENT_TYPE_MAP.get(extension);
            if (contentType != null) {
                return contentType;
            }
        }
        String fileContentType = file.getContentType();
        if (fileContentType != null && !fileContentType.isEmpty()) {
            return fileContentType;
        }
        return "application/octet-stream";
    }

    public InputStream downloadFile(String bucketName, String objectName) {
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
        } catch (Exception e) {
            log.error("文件下载失败: {}", e.getMessage());
            throw new RuntimeException("文件下载失败");
        }
    }

    public void deleteFile(String bucketName, String objectName) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage());
        }
    }

    public String getFileUrl(String bucketName, String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(7, TimeUnit.DAYS)
                    .build()
            );
        } catch (Exception e) {
            log.error("获取文件URL失败: {}", e.getMessage());
            return null;
        }
    }
}
