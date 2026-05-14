package com.smartsync.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartsync.api.dto.ApprovalLeaveDTO;
import com.smartsync.api.dto.ApprovalOvertimeDTO;
import com.smartsync.api.dto.ApprovalReimbursementDTO;
import com.smartsync.api.dto.ApprovalStatusMessage;
import com.smartsync.api.exception.BusinessException;
import com.smartsync.api.result.ResultCode;
import com.smartsync.auth.entity.*;
import com.smartsync.auth.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalMapper approvalMapper;
    private final ApprovalLeaveMapper approvalLeaveMapper;
    private final ApprovalOvertimeMapper approvalOvertimeMapper;
    private final ApprovalReimbursementMapper approvalReimbursementMapper;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final ApprovalMessageProducer messageProducer;

    private String generateApprovalNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return "AP" + timestamp + random;
    }

    private SysUser getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }

    @Transactional
    public Approval createLeaveApproval(ApprovalLeaveDTO dto) {
        SysUser currentUser = getCurrentUser();
        SysDept dept = deptMapper.selectById(currentUser.getDeptId());

        Approval approval = new Approval();
        approval.setApprovalNo(generateApprovalNo());
        approval.setApprovalType("LEAVE");
        approval.setTitle(dto.getLeaveType() + "申请");
        approval.setApplicantId(currentUser.getId());
        approval.setApplicantName(currentUser.getRealName());
        approval.setDeptId(currentUser.getDeptId());
        approval.setDeptName(dept != null ? dept.getDeptName() : "");
        approval.setStatus(0);
        approval.setIsDeleted(0);
        approval.setCreateTime(LocalDateTime.now());
        approval.setUpdateTime(LocalDateTime.now());
        approvalMapper.insert(approval);

        BigDecimal leaveDays = BigDecimal.valueOf(Duration.between(dto.getStartTime(), dto.getEndTime()).toHours() / 8.0);
        ApprovalLeave leave = new ApprovalLeave();
        leave.setApprovalId(approval.getId());
        leave.setLeaveType(dto.getLeaveType());
        leave.setStartTime(dto.getStartTime());
        leave.setEndTime(dto.getEndTime());
        leave.setLeaveDays(leaveDays);
        leave.setReason(dto.getReason());
        leave.setCreateTime(LocalDateTime.now());
        leave.setUpdateTime(LocalDateTime.now());
        leave.setIsDeleted(0);
        approvalLeaveMapper.insert(leave);

        return approval;
    }

    @Transactional
    public Approval createOvertimeApproval(ApprovalOvertimeDTO dto) {
        SysUser currentUser = getCurrentUser();
        SysDept dept = deptMapper.selectById(currentUser.getDeptId());

        Approval approval = new Approval();
        approval.setApprovalNo(generateApprovalNo());
        approval.setApprovalType("OVERTIME");
        approval.setTitle(dto.getOvertimeType() + "申请");
        approval.setApplicantId(currentUser.getId());
        approval.setApplicantName(currentUser.getRealName());
        approval.setDeptId(currentUser.getDeptId());
        approval.setDeptName(dept != null ? dept.getDeptName() : "");
        approval.setStatus(0);
        approval.setIsDeleted(0);
        approval.setCreateTime(LocalDateTime.now());
        approval.setUpdateTime(LocalDateTime.now());
        approvalMapper.insert(approval);

        BigDecimal overtimeHours = BigDecimal.valueOf(Duration.between(dto.getStartTime(), dto.getEndTime()).toHours());
        ApprovalOvertime overtime = new ApprovalOvertime();
        overtime.setApprovalId(approval.getId());
        overtime.setOvertimeType(dto.getOvertimeType());
        overtime.setStartTime(dto.getStartTime());
        overtime.setEndTime(dto.getEndTime());
        overtime.setOvertimeHours(overtimeHours);
        overtime.setReason(dto.getReason());
        overtime.setCreateTime(LocalDateTime.now());
        overtime.setUpdateTime(LocalDateTime.now());
        overtime.setIsDeleted(0);
        approvalOvertimeMapper.insert(overtime);

        return approval;
    }

    @Transactional
    public Approval createReimbursementApproval(ApprovalReimbursementDTO dto) {
        SysUser currentUser = getCurrentUser();
        SysDept dept = deptMapper.selectById(currentUser.getDeptId());

        Approval approval = new Approval();
        approval.setApprovalNo(generateApprovalNo());
        approval.setApprovalType("REIMBURSEMENT");
        approval.setTitle(dto.getExpenseType() + "报销申请");
        approval.setApplicantId(currentUser.getId());
        approval.setApplicantName(currentUser.getRealName());
        approval.setDeptId(currentUser.getDeptId());
        approval.setDeptName(dept != null ? dept.getDeptName() : "");
        approval.setStatus(0);
        approval.setIsDeleted(0);
        approval.setCreateTime(LocalDateTime.now());
        approval.setUpdateTime(LocalDateTime.now());
        approvalMapper.insert(approval);

        ApprovalReimbursement reimbursement = new ApprovalReimbursement();
        reimbursement.setApprovalId(approval.getId());
        reimbursement.setExpenseType(dto.getExpenseType());
        reimbursement.setTotalAmount(dto.getTotalAmount());
        reimbursement.setExpenseDate(dto.getExpenseDate());
        reimbursement.setReason(dto.getReason());
        reimbursement.setCreateTime(LocalDateTime.now());
        reimbursement.setUpdateTime(LocalDateTime.now());
        reimbursement.setIsDeleted(0);
        approvalReimbursementMapper.insert(reimbursement);

        return approval;
    }

    private void sendApprovalStatusMessage(Approval approval, Integer oldStatus, Integer newStatus) {
        ApprovalStatusMessage message = new ApprovalStatusMessage();
        message.setApprovalId(approval.getId());
        message.setApprovalNo(approval.getApprovalNo());
        message.setApprovalType(approval.getApprovalType());
        message.setTitle(approval.getTitle());
        message.setApplicantId(approval.getApplicantId());
        message.setApplicantName(approval.getApplicantName());
        message.setApproverId(approval.getApproverId());
        message.setApproverName(approval.getApproverName());
        message.setOldStatus(oldStatus);
        message.setNewStatus(newStatus);
        messageProducer.sendApprovalStatusChangeMessage(message);
    }

    @Transactional
    public void submitApproval(Long approvalId) {
        Approval approval = approvalMapper.selectById(approvalId);
        if (approval == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        if (approval.getStatus() != 0 && approval.getStatus() != 3) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "只有待提交或已驳回状态才能提交");
        }

        Integer oldStatus = approval.getStatus();
        approval.setStatus(1);
        approval.setSubmitTime(LocalDateTime.now());
        approval.setApproverId(null);
        approval.setApproverName(null);
        approval.setCurrentNode("DEPT_MANAGER");
        approval.setUpdateTime(LocalDateTime.now());
        approvalMapper.updateById(approval);

        ApprovalRecord record = new ApprovalRecord();
        record.setApprovalId(approvalId);
        record.setAction(oldStatus == 3 ? "RESUBMIT" : "SUBMIT");
        record.setOperateTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        record.setIsDeleted(0);
        approvalRecordMapper.insert(record);

        sendApprovalStatusMessage(approval, oldStatus, 1);
    }

    @Transactional
    public void approveApproval(Long approvalId, String opinion) {
        SysUser currentUser = getCurrentUser();
        Approval approval = approvalMapper.selectById(approvalId);
        if (approval == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        if (approval.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        Integer oldStatus = approval.getStatus();
        approval.setStatus(2);
        approval.setApproverId(currentUser.getId());
        approval.setApproverName(currentUser.getRealName());
        approval.setApproveTime(LocalDateTime.now());
        approval.setUpdateTime(LocalDateTime.now());
        approvalMapper.updateById(approval);

        ApprovalRecord record = new ApprovalRecord();
        record.setApprovalId(approvalId);
        record.setNode(approval.getCurrentNode());
        record.setApproverId(currentUser.getId());
        record.setApproverName(currentUser.getRealName());
        record.setAction("APPROVE");
        record.setOpinion(opinion);
        record.setOperateTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        record.setIsDeleted(0);
        approvalRecordMapper.insert(record);

        sendApprovalStatusMessage(approval, oldStatus, 2);
    }

    @Transactional
    public void rejectApproval(Long approvalId, String opinion) {
        SysUser currentUser = getCurrentUser();
        Approval approval = approvalMapper.selectById(approvalId);
        if (approval == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        if (approval.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        Integer oldStatus = approval.getStatus();
        approval.setStatus(3);
        approval.setApproverId(currentUser.getId());
        approval.setApproverName(currentUser.getRealName());
        approval.setApproveTime(LocalDateTime.now());
        approval.setUpdateTime(LocalDateTime.now());
        approvalMapper.updateById(approval);

        ApprovalRecord record = new ApprovalRecord();
        record.setApprovalId(approvalId);
        record.setNode(approval.getCurrentNode());
        record.setApproverId(currentUser.getId());
        record.setApproverName(currentUser.getRealName());
        record.setAction("REJECT");
        record.setOpinion(opinion);
        record.setOperateTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        record.setIsDeleted(0);
        approvalRecordMapper.insert(record);

        sendApprovalStatusMessage(approval, oldStatus, 3);
    }

    public Page<Approval> getMyApprovalPage(int page, int size, String approvalType, Integer status, LocalDateTime startTime, LocalDateTime endTime) {
        SysUser currentUser = getCurrentUser();
        Page<Approval> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Approval> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Approval::getApplicantId, currentUser.getId());
        if (approvalType != null && !approvalType.isEmpty()) {
            wrapper.eq(Approval::getApprovalType, approvalType);
        }
        if (status != null) {
            wrapper.eq(Approval::getStatus, status);
        }
        if (startTime != null) {
            wrapper.ge(Approval::getSubmitTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(Approval::getSubmitTime, endTime);
        }
        wrapper.orderByDesc(Approval::getCreateTime);
        return approvalMapper.selectPage(pageParam, wrapper);
    }

    public Page<Approval> getTodoApprovalPage(int page, int size, String approvalType, Integer status, LocalDateTime startTime, LocalDateTime endTime) {
        Page<Approval> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Approval> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Approval::getStatus, 1);
        if (approvalType != null && !approvalType.isEmpty()) {
            wrapper.eq(Approval::getApprovalType, approvalType);
        }
        if (status != null) {
            wrapper.eq(Approval::getStatus, status);
        }
        if (startTime != null) {
            wrapper.ge(Approval::getSubmitTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(Approval::getSubmitTime, endTime);
        }
        wrapper.orderByDesc(Approval::getSubmitTime);
        return approvalMapper.selectPage(pageParam, wrapper);
    }

    public Approval getApprovalDetail(Long approvalId) {
        return approvalMapper.selectById(approvalId);
    }

    public Object getApprovalBusinessDetail(Long approvalId, String approvalType) {
        switch (approvalType) {
            case "LEAVE":
                return approvalLeaveMapper.selectOne(new LambdaQueryWrapper<ApprovalLeave>().eq(ApprovalLeave::getApprovalId, approvalId));
            case "OVERTIME":
                return approvalOvertimeMapper.selectOne(new LambdaQueryWrapper<ApprovalOvertime>().eq(ApprovalOvertime::getApprovalId, approvalId));
            case "REIMBURSEMENT":
                return approvalReimbursementMapper.selectOne(new LambdaQueryWrapper<ApprovalReimbursement>().eq(ApprovalReimbursement::getApprovalId, approvalId));
            default:
                return null;
        }
    }

    public List<ApprovalRecord> getApprovalRecords(Long approvalId) {
        LambdaQueryWrapper<ApprovalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalRecord::getApprovalId, approvalId);
        wrapper.orderByAsc(ApprovalRecord::getOperateTime);
        return approvalRecordMapper.selectList(wrapper);
    }

    @Transactional
    public void urgeApproval(Long approvalId) {
        SysUser currentUser = getCurrentUser();
        Approval approval = approvalMapper.selectById(approvalId);
        if (approval == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        if (!approval.getApplicantId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "只能催办自己发起的审批");
        }
        if (approval.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "只有待审批状态才能催办");
        }

        ApprovalRecord record = new ApprovalRecord();
        record.setApprovalId(approvalId);
        record.setNode(approval.getCurrentNode());
        record.setApproverId(currentUser.getId());
        record.setApproverName(currentUser.getRealName());
        record.setAction("URGE");
        record.setOperateTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        record.setIsDeleted(0);
        approvalRecordMapper.insert(record);

        sendApprovalStatusMessage(approval, approval.getStatus(), approval.getStatus());
    }

    @Transactional
    public void withdrawApproval(Long approvalId, String reason) {
        SysUser currentUser = getCurrentUser();
        Approval approval = approvalMapper.selectById(approvalId);
        if (approval == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        if (!approval.getApplicantId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "只能撤回自己发起的审批");
        }
        if (approval.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "只有待审批状态才能撤回");
        }

        Integer oldStatus = approval.getStatus();
        approval.setStatus(4);
        approval.setUpdateTime(LocalDateTime.now());
        approvalMapper.updateById(approval);

        ApprovalRecord record = new ApprovalRecord();
        record.setApprovalId(approvalId);
        record.setNode(approval.getCurrentNode());
        record.setApproverId(currentUser.getId());
        record.setApproverName(currentUser.getRealName());
        record.setAction("WITHDRAW");
        record.setOpinion(reason);
        record.setOperateTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        record.setIsDeleted(0);
        approvalRecordMapper.insert(record);

        sendApprovalStatusMessage(approval, oldStatus, 4);
    }
}
