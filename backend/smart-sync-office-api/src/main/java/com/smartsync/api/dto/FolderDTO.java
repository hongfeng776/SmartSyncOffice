package com.smartsync.api.dto;

import lombok.Data;

@Data
public class FolderDTO {
    private Long id;
    private String folderName;
    private Long parentId;
    private Integer sort;
}
