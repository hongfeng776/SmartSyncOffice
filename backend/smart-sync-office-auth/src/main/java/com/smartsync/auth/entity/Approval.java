package com.smartsync.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("approval")
public class Approval {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String approvalNo;
    private String approvalType;
    private String title;
    private Long applicantId;
    private String applicantName;
    private Long deptId;
    private String deptName;
    private Long approverId;
    private String approverName;
    private Integer status;
    private String currentNode;
    private String remark;
    private LocalDateTime submitTime;
    private LocalDateTime approveTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
