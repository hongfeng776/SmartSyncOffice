package com.smartsync.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartsync.api.dto.TaskDTO;
import com.smartsync.api.result.Result;
import com.smartsync.auth.entity.Task;
import com.smartsync.auth.entity.TaskRecord;
import com.smartsync.auth.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public Result<Task> createTask(@RequestBody TaskDTO dto) {
        return Result.success(taskService.createTask(dto));
    }

    @PostMapping("/{id}/assign")
    public Result<Void> assignTask(@PathVariable Long id, @RequestParam Long assigneeId, @RequestParam(required = false) String comment) {
        taskService.assignTask(id, assigneeId, comment);
        return Result.success();
    }

    @PostMapping("/{id}/start")
    public Result<Void> startTask(@PathVariable Long id) {
        taskService.startTask(id);
        return Result.success();
    }

    @PostMapping("/{id}/progress")
    public Result<Void> updateTaskProgress(@PathVariable Long id, @RequestParam Integer progress, @RequestParam(required = false) String comment) {
        taskService.updateTaskProgress(id, progress, comment);
        return Result.success();
    }

    @PostMapping("/{id}/complete")
    public Result<Void> completeTask(@PathVariable Long id, @RequestParam(required = false) String comment) {
        taskService.completeTask(id, comment);
        return Result.success();
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancelTask(@PathVariable Long id, @RequestParam(required = false) String reason) {
        taskService.cancelTask(id, reason);
        return Result.success();
    }

    @GetMapping("/created")
    public Result<Map<String, Object>> getCreatedTasksPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime
    ) {
        Page<Task> taskPage = taskService.getCreatedTasksPage(page, size, status, priority, startTime, endTime);
        Map<String, Object> result = new HashMap<>();
        result.put("list", taskPage.getRecords());
        result.put("total", taskPage.getTotal());
        result.put("page", taskPage.getCurrent());
        result.put("size", taskPage.getSize());
        return Result.success(result);
    }

    @GetMapping("/assigned")
    public Result<Map<String, Object>> getAssignedTasksPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime
    ) {
        Page<Task> taskPage = taskService.getAssignedTasksPage(page, size, status, priority, startTime, endTime);
        Map<String, Object> result = new HashMap<>();
        result.put("list", taskPage.getRecords());
        result.put("total", taskPage.getTotal());
        result.put("page", taskPage.getCurrent());
        result.put("size", taskPage.getSize());
        return Result.success(result);
    }

    @GetMapping("/all")
    public Result<Map<String, Object>> getAllTasksPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime
    ) {
        Page<Task> taskPage = taskService.getAllTasksPage(page, size, status, priority, deptId, startTime, endTime);
        Map<String, Object> result = new HashMap<>();
        result.put("list", taskPage.getRecords());
        result.put("total", taskPage.getTotal());
        result.put("page", taskPage.getCurrent());
        result.put("size", taskPage.getSize());
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getTaskDetail(@PathVariable Long id) {
        Task task = taskService.getTaskDetail(id);
        List<TaskRecord> records = taskService.getTaskRecords(id);

        Map<String, Object> result = new HashMap<>();
        result.put("task", task);
        result.put("records", records);
        return Result.success(result);
    }
}
