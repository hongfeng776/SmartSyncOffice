package com.smartsync.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartsync.auth.entity.SysNotification;
import com.smartsync.auth.entity.SysUser;
import com.smartsync.auth.mapper.SysNotificationMapper;
import com.smartsync.auth.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SysNotificationMapper notificationMapper;
    private final SysUserMapper userMapper;
    private final Executor taskExecutor;

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        wrapper.select(SysUser::getId);
        SysUser user = userMapper.selectOne(wrapper);
        return user != null ? user.getId() : 1L;
    }

    public Page<SysNotification> getNotificationPage(int page, int size, Integer isRead, String type) {
        Page<SysNotification> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotification::getUserId, getCurrentUserId());
        if (isRead != null) {
            wrapper.eq(SysNotification::getIsRead, isRead);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(SysNotification::getType, type);
        }
        wrapper.orderByDesc(SysNotification::getCreateTime);
        return notificationMapper.selectPage(pageParam, wrapper);
    }

    @Transactional
    public void markAsRead(Long id) {
        SysNotification notification = notificationMapper.selectById(id);
        if (notification != null) {
            notification.setIsRead(1);
            notification.setReadTime(LocalDateTime.now());
            notificationMapper.updateById(notification);
        }
    }

    @Transactional
    public void markAllAsRead() {
        LambdaQueryWrapper<SysNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotification::getUserId, getCurrentUserId());
        wrapper.eq(SysNotification::getIsRead, 0);
        SysNotification update = new SysNotification();
        update.setIsRead(1);
        update.setReadTime(LocalDateTime.now());
        notificationMapper.update(update, wrapper);
    }

    public int getUnreadCount() {
        LambdaQueryWrapper<SysNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotification::getUserId, getCurrentUserId());
        wrapper.eq(SysNotification::getIsRead, 0);
        return Math.toIntExact(notificationMapper.selectCount(wrapper));
    }

    public Map<String, Integer> getUnreadCountByType() {
        LambdaQueryWrapper<SysNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotification::getUserId, getCurrentUserId());
        wrapper.eq(SysNotification::getIsRead, 0);
        List<SysNotification> notifications = notificationMapper.selectList(wrapper);
        return notifications.stream()
                .collect(Collectors.groupingBy(
                        SysNotification::getType,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }

    @Transactional
    public void sendNotification(Long userId, String title, String content, String type, String businessType, Long businessId) {
        SysNotification notification = new SysNotification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setBusinessType(businessType);
        notification.setBusinessId(businessId);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(notification);
        log.info("发送通知给用户: userId={}, title={}", userId, title);
    }

    @Async("taskExecutor")
    public void batchSendSystemNotification(String title, String content) {
        log.info("开始批量发送系统通知: title={}", title);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getStatus, 1);
        List<SysUser> users = userMapper.selectList(wrapper);
        
        int batchSize = 100;
        List<List<SysUser>> batches = users.stream()
                .collect(Collectors.groupingBy(e -> (int) (Math.random() * (users.size() / batchSize + 1))))
                .values().stream().toList();
        
        CountDownLatch latch = new CountDownLatch(batches.size());
        
        for (List<SysUser> batch : batches) {
            taskExecutor.execute(() -> {
                try {
                    for (SysUser user : batch) {
                        sendNotification(user.getId(), title, content, "SYSTEM", null, null);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        try {
            latch.await();
            log.info("批量发送系统通知完成，共发送{}条", users.size());
        } catch (InterruptedException e) {
            log.error("批量发送系统通知被中断", e);
            Thread.currentThread().interrupt();
        }
    }

    public void sendFileNotification(Long userId, String title, String content) {
        sendNotification(userId, title, content, "FILE", null, null);
    }

    public void sendApprovalNotification(Long userId, String title, String content, String approvalType, Long approvalId) {
        sendNotification(userId, title, content, "APPROVAL", approvalType, approvalId);
    }

    @Transactional
    public void deleteNotification(Long id) {
        SysNotification notification = notificationMapper.selectById(id);
        if (notification != null && notification.getUserId().equals(getCurrentUserId())) {
            notificationMapper.deleteById(id);
            log.info("删除消息: id={}, userId={}", id, getCurrentUserId());
        }
    }

    @Transactional
    public void markAllAsReadByType(String type) {
        LambdaQueryWrapper<SysNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotification::getUserId, getCurrentUserId());
        wrapper.eq(SysNotification::getIsRead, 0);
        if (type != null && !type.isEmpty()) {
            wrapper.eq(SysNotification::getType, type);
        }
        SysNotification update = new SysNotification();
        update.setIsRead(1);
        update.setReadTime(LocalDateTime.now());
        int count = notificationMapper.update(update, wrapper);
        log.info("标记全部已读: userId={}, type={}, count={}", getCurrentUserId(), type, count);
    }

    @Transactional
    public void clearReadNotifications() {
        LambdaQueryWrapper<SysNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotification::getUserId, getCurrentUserId());
        wrapper.eq(SysNotification::getIsRead, 1);
        int count = notificationMapper.delete(wrapper);
        log.info("清空已读消息: userId={}, count={}", getCurrentUserId(), count);
    }
}
