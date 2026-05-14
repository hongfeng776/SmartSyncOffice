package com.smartsync.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ApprovalOvertimeDTO {
    private String overtimeType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;
}
