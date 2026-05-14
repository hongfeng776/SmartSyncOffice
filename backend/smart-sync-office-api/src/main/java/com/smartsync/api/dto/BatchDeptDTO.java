package com.smartsync.api.dto;

import lombok.Data;
import java.util.List;

@Data
public class BatchDeptDTO {
    private Long newDeptId;
    private List<Long> employeeIds;
}