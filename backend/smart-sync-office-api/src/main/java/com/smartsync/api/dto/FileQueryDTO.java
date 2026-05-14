package com.smartsync.api.dto;

import lombok.Data;

@Data
public class FileQueryDTO {
    private String keyword;
    private String fileType;
    private String uploaderId;
    private Long folderId;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
