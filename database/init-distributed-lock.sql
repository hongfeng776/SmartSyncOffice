-- 分布式锁表初始化脚本

USE smart_sync_office;

DROP TABLE IF EXISTS sys_distributed_lock;
CREATE TABLE sys_distributed_lock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    lock_key VARCHAR(255) NOT NULL UNIQUE COMMENT '锁键',
    lock_value VARCHAR(255) NOT NULL COMMENT '锁值',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_lock_key (lock_key),
    INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分布式锁表';
