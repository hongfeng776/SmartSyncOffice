package com.smartsync.auth.utils;

import com.smartsync.auth.config.FileUploadConfig;
import com.smartsync.auth.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Component
public class FileUploadValidator {

    @Autowired
    private FileUploadConfig fileUploadConfig;

    private static final Pattern DOUBLE_EXTENSION_PATTERN = Pattern.compile(".*\\.[^.]+\\.[^.]+$");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[^a-zA-Z0-9.\\-_]");

    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new BusinessException("文件名不能为空");
        }

        validateFileName(originalFilename);
        validateFileSize(file.getSize());
        validateFileExtension(originalFilename);
        validateDoubleExtension(originalFilename);
        validateSpecialCharacters(originalFilename);
    }

    private void validateFileName(String fileName) {
        if (fileName.length() > 255) {
            throw new BusinessException("文件名长度不能超过255个字符");
        }

        if (fileName.startsWith(".") || fileName.endsWith(".")) {
            throw new BusinessException("文件名不能以点号开头或结尾");
        }

        if (fileName.contains("..")) {
            throw new BusinessException("文件名不能包含连续点号");
        }
    }

    private void validateFileSize(long fileSize) {
        Long maxFileSize = fileUploadConfig.getMaxFileSize();
        if (fileSize > maxFileSize) {
            String maxSizeStr = formatFileSize(maxFileSize);
            throw new BusinessException("文件大小超过限制，最大允许 " + maxSizeStr);
        }
    }

    private void validateFileExtension(String fileName) {
        String extension = getFileExtension(fileName);
        if (extension.isEmpty()) {
            throw new BusinessException("文件必须有扩展名");
        }

        List<String> forbiddenExtensions = fileUploadConfig.getForbiddenExtensions();
        if (forbiddenExtensions != null && !forbiddenExtensions.isEmpty()) {
            Set<String> forbiddenSet = new HashSet<>(forbiddenExtensions);
            if (forbiddenSet.contains(extension.toLowerCase(Locale.ROOT))) {
                log.warn("检测到禁止的文件类型: {} - {}", fileName, extension);
                throw new BusinessException("禁止上传的文件类型: " + extension);
            }
        }

        List<String> allowedExtensions = fileUploadConfig.getAllowedExtensions();
        if (allowedExtensions != null && !allowedExtensions.isEmpty()) {
            Set<String> allowedSet = new HashSet<>(allowedExtensions);
            if (!allowedSet.contains(extension.toLowerCase(Locale.ROOT))) {
                log.warn("检测到不允许的文件类型: {} - {}", fileName, extension);
                throw new BusinessException("不支持的文件类型: " + extension);
            }
        }
    }

    private void validateDoubleExtension(String fileName) {
        String lowerFileName = fileName.toLowerCase(Locale.ROOT);
        List<String> forbiddenExtensions = fileUploadConfig.getForbiddenExtensions();
        
        if (forbiddenExtensions != null) {
            for (String forbiddenExt : forbiddenExtensions) {
                int firstIndex = lowerFileName.indexOf("." + forbiddenExt);
                if (firstIndex != -1 && firstIndex < lowerFileName.lastIndexOf(".")) {
                    log.warn("检测到双重扩展名恶意文件: {}", fileName);
                    throw new BusinessException("文件格式不安全，请检查文件扩展名");
                }
            }
        }
    }

    private void validateSpecialCharacters(String fileName) {
        if (SPECIAL_CHAR_PATTERN.matcher(fileName).find()) {
            throw new BusinessException("文件名不能包含特殊字符");
        }
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}
