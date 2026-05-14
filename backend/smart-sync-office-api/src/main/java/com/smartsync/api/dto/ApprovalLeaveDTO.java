package com.smartsync.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ApprovalLeaveDTO {
    private String leaveType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
 private String reason;
}
