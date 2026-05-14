package com.smartsync.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_file_upload")
public class SysFileUpload {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String uploadId;

    private String filename;

    private Long totalSize;

    private Integer totalChunks;

    private Integer uploadedChunks;

    private String uploadedChunkNumbers;

    private Long folderId;

    private String uploaderId;

    private String uploaderName;

    private String status;

    private String storedPath;

    private String md5;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
