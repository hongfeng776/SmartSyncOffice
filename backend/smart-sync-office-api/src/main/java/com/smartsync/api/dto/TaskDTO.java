package com.smartsync.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private Integer priority;
    private String taskType;
    private Long assigneeId;
    private String assigneeName;
    private LocalDateTime deadline;
    private String remark;
}
