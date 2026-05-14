package com.smartsync.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("approval_overtime")
public class ApprovalOvertime {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long approvalId;
    private String overtimeType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal overtimeHours;
    private String reason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
