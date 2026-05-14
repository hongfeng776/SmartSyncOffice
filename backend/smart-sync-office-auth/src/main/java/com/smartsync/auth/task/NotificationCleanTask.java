package com.smartsync.auth.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartsync.auth.entity.SysNotification;
import com.smartsync.auth.mapper.SysNotificationMapper;
import com.smartsync.auth.service.DistributedLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCleanTask {

    private final SysNotificationMapper notificationMapper;
    private final DistributedLockService distributedLockService;

    private static final String CLEAN_LOCK_KEY = "notification:clean:lock";
    private static final long LOCK_EXPIRE_SECONDS = 1800;
    private static final int BATCH_SIZE = 100;
    private static final int MAX_DELETE_PER_RUN = 10000;
    
    private static final int SYSTEM_EXPIRE_DAYS = 30;
    private static final int APPROVAL_EXPIRE_DAYS = 60;
    private static final int FILE_EXPIRE_DAYS = 45;

    @Async("taskExecutor")
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void cleanExpiredNotifications() {
        long startTime = System.currentTimeMillis();
        log.info("========== 开始消息过期清理任务 ==========");

        boolean locked = distributedLockService.tryLock(CLEAN_LOCK_KEY, LOCK_EXPIRE_SECONDS);
        if (!locked) {
            log.info("消息清理任务已在其他节点执行，跳过本次执行");
            return;
        }

        log.info("成功获取分布式锁，开始执行清理");

        int totalDeleted = 0;
        try {
            totalDeleted += cleanByType("SYSTEM", SYSTEM_EXPIRE_DAYS);
            totalDeleted += cleanByType("APPROVAL", APPROVAL_EXPIRE_DAYS);
            totalDeleted += cleanByType("FILE", FILE_EXPIRE_DAYS);
            
            long costTime = System.currentTimeMillis() - startTime;
            log.info("========== 消息清理完成，共清理{}条，耗时{}ms ==========", totalDeleted, costTime);
        } catch (Exception e) {
            log.error("消息过期清理任务执行失败，已删除消息数: {}", totalDeleted, e);
        } finally {
            distributedLockService.unlock(CLEAN_LOCK_KEY);
        }
    }

    private int cleanByType(String type, int days) {
        int deletedCount = 0;
        LocalDateTime expireTime = LocalDateTime.now().minusDays(days);
        
        log.info("开始清理{}类型消息，过期时间: {}（保留{}天）", type, expireTime, days);

        try {
            while (deletedCount < MAX_DELETE_PER_RUN) {
                LambdaQueryWrapper<SysNotification> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(SysNotification::getType, type);
                queryWrapper.lt(SysNotification::getCreateTime, expireTime);
                queryWrapper.last("LIMIT " + BATCH_SIZE);
                queryWrapper.select(SysNotification::getId);

                List<SysNotification> toDelete = notificationMapper.selectList(queryWrapper);
                if (toDelete.isEmpty()) {
                    break;
                }

                List<Long> idsToDelete = toDelete.stream()
                    .map(SysNotification::getId)
                    .collect(Collectors.toList());

                LambdaQueryWrapper<SysNotification> deleteWrapper = new LambdaQueryWrapper<>();
                deleteWrapper.in(SysNotification::getId, idsToDelete);
                deleteWrapper.eq(SysNotification::getType, type);
                deleteWrapper.lt(SysNotification::getCreateTime, expireTime);
                
                int deleted = notificationMapper.delete(deleteWrapper);
                deletedCount += deleted;

                log.debug("批量删除{}类型消息: {}条，累计已删除: {}", type, deleted, deletedCount);

                if (deleted < BATCH_SIZE) {
                    break;
                }

                Thread.sleep(100);
            }

            log.info("{}类型消息清理完成，共删除{}条", type, deletedCount);
        } catch (InterruptedException e) {
            log.warn("消息清理被中断，已删除{}条{}消息", deletedCount, type);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("清理{}类型消息失败，已删除{}条", type, deletedCount, e);
        }

        return deletedCount;
    }

    @Async("taskExecutor")
    @Scheduled(cron = "0 30 3 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void cleanExpiredLocks() {
        log.info("开始清理分布式锁表的过期记录");
        distributedLockService.cleanExpiredLocks();
        log.info("分布式锁表清理完成");
    }
}
