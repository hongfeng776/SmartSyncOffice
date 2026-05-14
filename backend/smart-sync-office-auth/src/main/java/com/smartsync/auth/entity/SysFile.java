package com.smartsync.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_file")
public class SysFile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String originalFilename;

    private String storedFilename;

    private String filePath;

    private String fileSize;

    private String fileType;

    private String contentType;

    private String fileExtension;

    private String bucketName;

    private Long folderId;

    private String uploaderId;

    private String uploaderName;

    private Integer downloadCount;

    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
