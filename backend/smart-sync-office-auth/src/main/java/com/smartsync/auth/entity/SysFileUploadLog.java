package com.smartsync.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_file_upload_log")
public class SysFileUploadLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String originalFilename;

    private Long fileSize;

    private String fileExtension;

    private String contentType;

    private Long folderId;

    private String uploaderId;

    private String uploaderName;

    private String uploaderIp;

    private String userAgent;

    private String uploadStatus;

    private String errorMessage;

    private String storedFilename;

    private String filePath;

    private String bucketName;

    private Long fileId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
