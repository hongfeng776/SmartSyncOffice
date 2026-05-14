package com.smartsync.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartsync.auth.entity.SysAnnouncement;
import com.smartsync.auth.entity.SysUser;
import com.smartsync.auth.mapper.SysAnnouncementMapper;
import com.smartsync.auth.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final SysAnnouncementMapper announcementMapper;
    private final SysUserMapper userMapper;
    private final NotificationService notificationService;
    private final Executor taskExecutor;

    public Page<SysAnnouncement> getAnnouncementPage(int page, int size, Integer status) {
        Page<SysAnnouncement> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysAnnouncement> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(SysAnnouncement::getStatus, status);
        }
        wrapper.orderByDesc(SysAnnouncement::getPublishTime);
        wrapper.orderByDesc(SysAnnouncement::getPriority);
        return announcementMapper.selectPage(pageParam, wrapper);
    }

    public SysAnnouncement getAnnouncementById(Long id) {
        return announcementMapper.selectById(id);
    }

    @Transactional
    public void createAnnouncement(SysAnnouncement announcement) {
        announcement.setCreateTime(LocalDateTime.now());
        announcement.setStatus(0);
        announcementMapper.insert(announcement);
        log.info("创建系统公告: id={}, title={}", announcement.getId(), announcement.getTitle());
    }

    @Transactional
    public void updateAnnouncement(SysAnnouncement announcement) {
        announcement.setUpdateTime(LocalDateTime.now());
        announcementMapper.updateById(announcement);
        log.info("更新系统公告: id={}", announcement.getId());
    }

    @Transactional
    public void publishAnnouncement(Long id) {
        SysAnnouncement announcement = announcementMapper.selectById(id);
        if (announcement != null) {
            announcement.setStatus(1);
            announcement.setPublishTime(LocalDateTime.now());
            announcement.setUpdateTime(LocalDateTime.now());
            announcementMapper.updateById(announcement);
            log.info("发布系统公告: id={}, title={}", id, announcement.getTitle());
            batchSendAnnouncement(announcement);
        }
    }

    @Async("taskExecutor")
    public void batchSendAnnouncement(SysAnnouncement announcement) {
        log.info("开始批量推送公告通知: announcementId={}", announcement.getId());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getStatus, 1);
        List<SysUser> users = userMapper.selectList(wrapper);
        
        int batchSize = 100;
        int batchCount = (users.size() + batchSize - 1) / batchSize;
        CountDownLatch latch = new CountDownLatch(batchCount);
        
        for (int i = 0; i < batchCount; i++) {
            int start = i * batchSize;
            int end = Math.min(start + batchSize, users.size());
            List<SysUser> batch = users.subList(start, end);
            
            taskExecutor.execute(() -> {
                try {
                    for (SysUser user : batch) {
                        try {
                            notificationService.sendNotification(
                                user.getId(),
                                announcement.getTitle(),
                                announcement.getContent(),
                                "SYSTEM",
                                "ANNOUNCEMENT",
                                announcement.getId()
                            );
                        } catch (Exception e) {
                            log.error("发送公告通知失败: userId={}", user.getId(), e);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        try {
            latch.await();
            log.info("批量推送公告通知完成，共发送{}条", users.size());
        } catch (InterruptedException e) {
            log.error("批量推送公告通知被中断", e);
            Thread.currentThread().interrupt();
        }
    }

    @Transactional
    public void withdrawAnnouncement(Long id) {
        SysAnnouncement announcement = announcementMapper.selectById(id);
        if (announcement != null) {
            announcement.setStatus(2);
            announcement.setUpdateTime(LocalDateTime.now());
            announcementMapper.updateById(announcement);
            log.info("撤回系统公告: id={}", id);
        }
    }

    @Transactional
    public void deleteAnnouncement(Long id) {
        announcementMapper.deleteById(id);
        log.info("删除系统公告: id={}", id);
    }
}
