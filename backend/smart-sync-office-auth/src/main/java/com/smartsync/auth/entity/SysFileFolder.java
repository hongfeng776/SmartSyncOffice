package com.smartsync.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("sys_file_folder")
public class SysFileFolder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String folderName;

    private Long parentId;

    private String folderPath;

    private Integer level;

    private Integer sort;

    private String creatorId;

    private String creatorName;

    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private List<SysFileFolder> children;
}
