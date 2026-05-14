package com.smartsync.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartsync.api.dto.TaskDTO;
import com.smartsync.api.exception.BusinessException;
import com.smartsync.api.result.ResultCode;
import com.smartsync.auth.entity.*;
import com.smartsync.auth.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskMapper taskMapper;
    private final TaskRecordMapper taskRecordMapper;
    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final SysNotificationMapper notificationMapper;

    private String generateTaskNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return "TK" + timestamp + random;
    }

    private SysUser getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }

    private void createTaskRecord(Long taskId, String action, Integer oldStatus, Integer newStatus, String comment, Integer progress) {
        SysUser currentUser = getCurrentUser();
        TaskRecord record = new TaskRecord();
        record.setTaskId(taskId);
        record.setOperatorId(currentUser.getId());
        record.setOperatorName(currentUser.getRealName());
        record.setAction(action);
        record.setOldStatus(oldStatus);
        record.setNewStatus(newStatus);
        record.setComment(comment);
        record.setProgress(progress);
        record.setOperateTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        record.setIsDeleted(0);
        taskRecordMapper.insert(record);
    }

    @Transactional
    public Task createTask(TaskDTO dto) {
        SysUser currentUser = getCurrentUser();
        SysDept dept = deptMapper.selectById(currentUser.getDeptId());

        Task task = new Task();
        task.setTaskNo(generateTaskNo());
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setPriority(dto.getPriority() != null ? dto.getPriority() : 1);
        task.setTaskType(dto.getTaskType());
        task.setCreatorId(currentUser.getId());
        task.setCreatorName(currentUser.getRealName());
        task.setDeptId(currentUser.getDeptId());
        task.setDeptName(dept != null ? dept.getDeptName() : "");
        task.setDeadline(dto.getDeadline());
        task.setRemark(dto.getRemark());
        task.setProgress(0);
        task.setRemindSent(0);
        task.setOverdueRemindSent(0);
        task.setIsDeleted(0);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

        if (dto.getAssigneeId() != null) {
            SysUser assignee = userMapper.selectById(dto.getAssigneeId());
            if (assignee != null) {
                task.setAssigneeId(assignee.getId());
                task.setAssigneeName(assignee.getRealName());
                task.setStatus(1);
            } else {
                task.setStatus(0);
            }
        } else {
            task.setStatus(0);
        }

        taskMapper.insert(task);

        createTaskRecord(task.getId(), "CREATE", null, task.getStatus(), "创建任务", null);

        if (dto.getAssigneeId() != null && task.getStatus() == 1) {
            createTaskRecord(task.getId(), "ASSIGN", 0, 1, "指派任务给: " + task.getAssigneeName(), null);
        }

        return task;
    }

    @Transactional
    public void assignTask(Long taskId, Long assigneeId, String comment) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        SysUser assignee = userMapper.selectById(assigneeId);
        if (assignee == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "执行人不存在");
        }

        Integer oldStatus = task.getStatus();
        task.setAssigneeId(assigneeId);
        task.setAssigneeName(assignee.getRealName());
        task.setStatus(1);
        task.setRemindSent(0);
        task.setOverdueRemindSent(0);
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);

        createTaskRecord(taskId, "ASSIGN", oldStatus, 1, comment, null);

        SysNotification notification = new SysNotification();
        notification.setUserId(assigneeId);
        notification.setTitle("新任务指派");
        notification.setContent(String.format("您有一个新的任务待执行：%s", task.getTitle()));
        notification.setType("TASK_ASSIGN");
        notification.setBusinessType("TASK");
        notification.setBusinessId(taskId);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(notification);
    }

    @Transactional
    public void startTask(Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        if (task.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "只有待执行状态才能开始任务");
        }

        SysUser currentUser = getCurrentUser();
        if (!task.getAssigneeId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "只有任务执行人才能开始任务");
        }

        Integer oldStatus = task.getStatus();
        task.setStatus(2);
        task.setStartTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);

        createTaskRecord(taskId, "START", oldStatus, 2, "开始执行任务", null);
    }

    @Transactional
    public void updateTaskProgress(Long taskId, Integer progress, String comment) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        if (task.getStatus() != 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "只有进行中状态才能更新进度");
        }

        SysUser currentUser = getCurrentUser();
        if (!task.getAssigneeId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "只有任务执行人才能更新进度");
        }

        if (progress < 0 || progress > 100) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "进度值必须在0-100之间");
        }

        Integer oldProgress = task.getProgress();
        task.setProgress(progress);
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);

        createTaskRecord(taskId, "UPDATE", task.getStatus(), task.getStatus(), comment, progress - oldProgress);
    }

    @Transactional
    public void completeTask(Long taskId, String comment) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        if (task.getStatus() != 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "只有进行中状态才能完成任务");
        }

        SysUser currentUser = getCurrentUser();
        if (!task.getAssigneeId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "只有任务执行人才能完成任务");
        }

        Integer oldStatus = task.getStatus();
        task.setStatus(3);
        task.setProgress(100);
        task.setCompleteTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);

        createTaskRecord(taskId, "COMPLETE", oldStatus, 3, comment, 100 - task.getProgress());
    }

    @Transactional
    public void cancelTask(Long taskId, String reason) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        if (task.getStatus() == 3 || task.getStatus() == 4) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "已完成或已取消的任务不能取消");
        }

        SysUser currentUser = getCurrentUser();
        if (!task.getCreatorId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "只有任务创建人才能取消任务");
        }

        Integer oldStatus = task.getStatus();
        task.setStatus(4);
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);

        createTaskRecord(taskId, "CANCEL", oldStatus, 4, reason, null);
    }

    public Page<Task> getCreatedTasksPage(int page, int size, Integer status, Integer priority, LocalDateTime startTime, LocalDateTime endTime) {
        SysUser currentUser = getCurrentUser();
        Page<Task> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getCreatorId, currentUser.getId());
        if (status != null) {
            wrapper.eq(Task::getStatus, status);
        }
        if (priority != null) {
            wrapper.eq(Task::getPriority, priority);
        }
        if (startTime != null) {
            wrapper.ge(Task::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(Task::getCreateTime, endTime);
        }
        wrapper.orderByDesc(Task::getCreateTime);
        return taskMapper.selectPage(pageParam, wrapper);
    }

    public Page<Task> getAssignedTasksPage(int page, int size, Integer status, Integer priority, LocalDateTime startTime, LocalDateTime endTime) {
        SysUser currentUser = getCurrentUser();
        Page<Task> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getAssigneeId, currentUser.getId());
        if (status != null) {
            wrapper.eq(Task::getStatus, status);
        }
        if (priority != null) {
            wrapper.eq(Task::getPriority, priority);
        }
        if (startTime != null) {
            wrapper.ge(Task::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(Task::getCreateTime, endTime);
        }
        wrapper.orderByDesc(Task::getCreateTime);
        return taskMapper.selectPage(pageParam, wrapper);
    }

    public Page<Task> getAllTasksPage(int page, int size, Integer status, Integer priority, Long deptId, LocalDateTime startTime, LocalDateTime endTime) {
        Page<Task> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Task::getStatus, status);
        }
        if (priority != null) {
            wrapper.eq(Task::getPriority, priority);
        }
        if (deptId != null) {
            wrapper.eq(Task::getDeptId, deptId);
        }
        if (startTime != null) {
            wrapper.ge(Task::getDeadline, startTime);
        }
        if (endTime != null) {
            wrapper.le(Task::getDeadline, endTime);
        }
        wrapper.orderByDesc(Task::getCreateTime);
        return taskMapper.selectPage(pageParam, wrapper);
    }

    public List<Task> getUrgentTasks(int hoursBeforeDeadline) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadlineThreshold = now.plusHours(hoursBeforeDeadline);
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Task::getStatus, 1, 2)
               .ge(Task::getDeadline, now)
               .le(Task::getDeadline, deadlineThreshold)
               .orderByAsc(Task::getDeadline);
        return taskMapper.selectList(wrapper);
    }

    public List<Task> getOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Task::getStatus, 1, 2)
               .lt(Task::getDeadline, now)
               .orderByAsc(Task::getDeadline);
        return taskMapper.selectList(wrapper);
    }

    public Task getTaskDetail(Long taskId) {
        return taskMapper.selectById(taskId);
    }

    public List<TaskRecord> getTaskRecords(Long taskId) {
        LambdaQueryWrapper<TaskRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskRecord::getTaskId, taskId);
        wrapper.orderByAsc(TaskRecord::getOperateTime);
        return taskRecordMapper.selectList(wrapper);
    }
}
