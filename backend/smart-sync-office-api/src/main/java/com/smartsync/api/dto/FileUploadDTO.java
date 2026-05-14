package com.smartsync.api.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileUploadDTO {
    private MultipartFile file;
    private String uploaderId;
    private String uploaderName;
}
