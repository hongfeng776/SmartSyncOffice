package com.smartsync.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("approval_record")
public class ApprovalRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long approvalId;
    private String node;
    private Long approverId;
    private String approverName;
    private String action;
    private String opinion;
    private LocalDateTime operateTime;
    private LocalDateTime createTime;
    private Integer isDeleted;
}
