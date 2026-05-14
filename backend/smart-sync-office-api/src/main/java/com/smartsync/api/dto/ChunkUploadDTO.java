package com.smartsync.api.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ChunkUploadDTO {
    private MultipartFile file;
    private String uploadId;
    private Integer chunkNumber;
    private Integer totalChunks;
    private Long totalSize;
    private String filename;
    private Long folderId;
    private Long uploaderId;
    private String uploaderName;
}
