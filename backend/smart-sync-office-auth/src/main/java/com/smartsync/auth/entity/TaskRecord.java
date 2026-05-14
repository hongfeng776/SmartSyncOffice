package com.smartsync.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task_record")
public class TaskRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long operatorId;
    private String operatorName;
    private String action;
    private Integer oldStatus;
    private Integer newStatus;
    private String comment;
    private Integer progress;
    private LocalDateTime operateTime;
    private LocalDateTime createTime;
    private Integer isDeleted;
}
