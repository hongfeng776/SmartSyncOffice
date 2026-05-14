package com.smartsync.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ApprovalReimbursementDTO {
    private String expenseType;
    private BigDecimal totalAmount;
    private LocalDate expenseDate;
    private String reason;
}
