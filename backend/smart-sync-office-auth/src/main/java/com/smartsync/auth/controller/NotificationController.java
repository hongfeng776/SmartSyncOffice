package com.smartsync.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartsync.api.result.Result;
import com.smartsync.auth.entity.SysNotification;
import com.smartsync.auth.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public Result<Map<String, Object>> getNotificationPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer isRead,
            @RequestParam(required = false) String type
    ) {
        Page<SysNotification> notificationPage = notificationService.getNotificationPage(page, size, isRead, type);
        Map<String, Object> result = new HashMap<>();
        result.put("list", notificationPage.getRecords());
        result.put("total", notificationPage.getTotal());
        result.put("page", notificationPage.getCurrent());
        result.put("size", notificationPage.getSize());
        return Result.success(result);
    }

    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount() {
        return Result.success(notificationService.getUnreadCount());
    }

    @GetMapping("/unread-count-by-type")
    public Result<Map<String, Integer>> getUnreadCountByType() {
        return Result.success(notificationService.getUnreadCountByType());
    }

    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success();
    }

    @PutMapping("/read-all")
    public Result<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return Result.success();
    }

    @PostMapping("/batch-send")
    public Result<Void> batchSendSystemNotification(
            @RequestParam String title,
            @RequestParam String content
    ) {
        notificationService.batchSendSystemNotification(title, content);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return Result.success();
    }

    @PutMapping("/read-all-by-type")
    public Result<Void> markAllAsReadByType(@RequestParam(required = false) String type) {
        notificationService.markAllAsReadByType(type);
        return Result.success();
    }

    @DeleteMapping("/clear-read")
    public Result<Void> clearReadNotifications() {
        notificationService.clearReadNotifications();
        return Result.success();
    }
}
