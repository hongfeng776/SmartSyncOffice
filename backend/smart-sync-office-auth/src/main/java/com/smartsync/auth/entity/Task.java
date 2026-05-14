package com.smartsync.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task")
public class Task {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskNo;
    private String title;
    private String description;
    private Integer priority;
    private String taskType;
    private Long creatorId;
    private String creatorName;
    private Long assigneeId;
    private String assigneeName;
    private Long deptId;
    private String deptName;
    private Integer status;
    private Integer progress;
    private LocalDateTime deadline;
    private LocalDateTime startTime;
    private LocalDateTime completeTime;
    private String remark;
    private Integer remindSent;
    private Integer overdueRemindSent;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
