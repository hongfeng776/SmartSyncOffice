package com.smartsync.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartsync.api.dto.ApprovalLeaveDTO;
import com.smartsync.api.dto.ApprovalOvertimeDTO;
import com.smartsync.api.dto.ApprovalReimbursementDTO;
import com.smartsync.api.result.Result;
import com.smartsync.auth.entity.Approval;
import com.smartsync.auth.entity.ApprovalRecord;
import com.smartsync.auth.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @PostMapping("/leave")
    public Result<Approval> createLeaveApproval(@RequestBody ApprovalLeaveDTO dto) {
        return Result.success(approvalService.createLeaveApproval(dto));
    }

    @PostMapping("/overtime")
    public Result<Approval> createOvertimeApproval(@RequestBody ApprovalOvertimeDTO dto) {
        return Result.success(approvalService.createOvertimeApproval(dto));
    }

    @PostMapping("/reimbursement")
    public Result<Approval> createReimbursementApproval(@RequestBody ApprovalReimbursementDTO dto) {
        return Result.success(approvalService.createReimbursementApproval(dto));
    }

    @PostMapping("/{id}/submit")
    public Result<Void> submitApproval(@PathVariable Long id) {
        approvalService.submitApproval(id);
        return Result.success();
    }

    @PostMapping("/{id}/approve")
    public Result<Void> approveApproval(@PathVariable Long id, @RequestParam(required = false) String opinion) {
        approvalService.approveApproval(id, opinion);
        return Result.success();
    }

    @PostMapping("/{id}/reject")
    public Result<Void> rejectApproval(@PathVariable Long id, @RequestParam(required = false) String opinion) {
        approvalService.rejectApproval(id, opinion);
        return Result.success();
    }

    @PostMapping("/{id}/urge")
    public Result<Void> urgeApproval(@PathVariable Long id) {
        approvalService.urgeApproval(id);
        return Result.success();
    }

    @PostMapping("/{id}/withdraw")
    public Result<Void> withdrawApproval(@PathVariable Long id, @RequestParam(required = false) String reason) {
        approvalService.withdrawApproval(id, reason);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<Map<String, Object>> getMyApprovalPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String approvalType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime
    ) {
        Page<Approval> approvalPage = approvalService.getMyApprovalPage(page, size, approvalType, status, startTime, endTime);
        Map<String, Object> result = new HashMap<>();
        result.put("list", approvalPage.getRecords());
        result.put("total", approvalPage.getTotal());
        result.put("page", approvalPage.getCurrent());
        result.put("size", approvalPage.getSize());
        return Result.success(result);
    }

    @GetMapping("/todo")
    public Result<Map<String, Object>> getTodoApprovalPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String approvalType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime
    ) {
        Page<Approval> approvalPage = approvalService.getTodoApprovalPage(page, size, approvalType, status, startTime, endTime);
        Map<String, Object> result = new HashMap<>();
        result.put("list", approvalPage.getRecords());
        result.put("total", approvalPage.getTotal());
        result.put("page", approvalPage.getCurrent());
        result.put("size", approvalPage.getSize());
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getApprovalDetail(@PathVariable Long id) {
        Approval approval = approvalService.getApprovalDetail(id);
        Object businessDetail = approvalService.getApprovalBusinessDetail(id, approval.getApprovalType());
        List<ApprovalRecord> records = approvalService.getApprovalRecords(id);

        Map<String, Object> result = new HashMap<>();
        result.put("approval", approval);
        result.put("businessDetail", businessDetail);
        result.put("records", records);
        return Result.success(result);
    }
}
