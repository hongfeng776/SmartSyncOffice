package com.smartsync.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApprovalStatusMessage implements Serializable {
    private Long approvalId;
    private String approvalNo;
    private String approvalType;
    private String title;
    private Long applicantId;
    private String applicantName;
    private Long approverId;
    private String approverName;
    private Integer oldStatus;
    private Integer newStatus;
    private String remark;
}
