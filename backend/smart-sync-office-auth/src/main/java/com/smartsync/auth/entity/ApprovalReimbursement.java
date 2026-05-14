package com.smartsync.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("approval_reimbursement")
public class ApprovalReimbursement {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long approvalId;
    private String expenseType;
    private BigDecimal totalAmount;
    private LocalDate expenseDate;
    private String reason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
