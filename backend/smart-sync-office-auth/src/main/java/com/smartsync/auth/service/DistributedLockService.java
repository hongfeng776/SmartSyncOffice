package com.smartsync.auth.service;

import com.smartsync.auth.entity.SysDistributedLock;
import com.smartsync.auth.mapper.SysDistributedLockMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockService {

    private final SysDistributedLockMapper lockMapper;
    private final ThreadLocal<String> lockValueHolder = new ThreadLocal<>();

    /**
     * 尝试获取分布式锁
     * @param lockKey 锁键
     * @param expireSeconds 过期时间（秒）
     * @return 是否获取成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean tryLock(String lockKey, long expireSeconds) {
        String lockValue = UUID.randomUUID().toString();
        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(expireSeconds);

        try {
            SysDistributedLock existingLock = lockMapper.selectByKeyForUpdate(lockKey);

            if (existingLock == null) {
                try {
                    int inserted = lockMapper.insertLock(lockKey, lockValue, expireTime);
                    if (inserted > 0) {
                        lockValueHolder.set(lockValue);
                        log.info("获取锁成功: {}, 锁值: {}", lockKey, lockValue);
                        return true;
                    }
                } catch (DuplicateKeyException e) {
                    log.warn("锁已被其他节点抢占: {}", lockKey);
                    return false;
                }
                return false;
            }

            if (existingLock.getExpireTime().isBefore(LocalDateTime.now())) {
                log.info("锁已过期，尝试更新: {}, 旧锁值: {}", lockKey, existingLock.getLockValue());
                int updated = lockMapper.updateLock(lockKey, lockValue, expireTime, existingLock.getLockValue());
                if (updated > 0) {
                    lockValueHolder.set(lockValue);
                    log.info("更新过期锁成功: {}, 新锁值: {}", lockKey, lockValue);
                    return true;
                }
                return false;
            }

            log.debug("锁已被占用: {}, 剩余时间: {} 秒", lockKey, 
                java.time.Duration.between(LocalDateTime.now(), existingLock.getExpireTime()).getSeconds());
            return false;
        } catch (Exception e) {
            log.error("获取锁异常: {}", lockKey, e);
            return false;
        }
    }

    /**
     * 释放分布式锁（只有锁的持有者才能释放）
     * @param lockKey 锁键
     */
    @Transactional(rollbackFor = Exception.class)
    public void unlock(String lockKey) {
        String currentLockValue = lockValueHolder.get();
        if (currentLockValue == null) {
            log.debug("当前线程未持有锁: {}", lockKey);
            return;
        }

        try {
            int deleted = lockMapper.deleteLock(lockKey, currentLockValue);
            if (deleted > 0) {
                log.info("释放锁成功: {}, 锁值: {}", lockKey, currentLockValue);
            } else {
                log.warn("释放锁失败，可能锁已过期或被其他节点抢占: {}, 锁值: {}", lockKey, currentLockValue);
            }
        } catch (Exception e) {
            log.error("释放锁异常: {}", lockKey, e);
        } finally {
            lockValueHolder.remove();
        }
    }

    /**
     * 清理所有过期的锁记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void cleanExpiredLocks() {
        try {
            int count = lockMapper.deleteExpiredLocks();
            if (count > 0) {
                log.info("清理过期锁记录: {} 条", count);
            }
        } catch (Exception e) {
            log.warn("清理过期锁异常", e);
        }
    }
}
