-- 消息通知相关表初始化脚本

USE smart_sync_office;

-- 消息通知表
DROP TABLE IF EXISTS sys_notification;
CREATE TABLE sys_notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    title VARCHAR(200) NOT NULL COMMENT '消息标题',
    content TEXT COMMENT '消息内容',
    type VARCHAR(50) NOT NULL COMMENT '消息类型: SYSTEM-系统通知, APPROVAL-审批通知, FILE-文件动态',
    business_type VARCHAR(50) COMMENT '业务类型',
    business_id BIGINT COMMENT '业务ID',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
    read_time DATETIME COMMENT '阅读时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_create_time (create_time),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- 系统公告表
DROP TABLE IF EXISTS sys_announcement;
CREATE TABLE sys_announcement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '公告ID',
    title VARCHAR(200) NOT NULL COMMENT '公告标题',
    content TEXT COMMENT '公告内容',
    priority INT DEFAULT 0 COMMENT '优先级: 0-普通, 1-重要, 2-紧急',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-草稿, 1-已发布, 2-已撤回',
    publish_time DATETIME COMMENT '发布时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    INDEX idx_status (status),
    INDEX idx_publish_time (publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';

-- 插入示例数据
INSERT INTO sys_notification (user_id, title, content, type, business_type, business_id, is_read, create_time) VALUES
(1, '欢迎使用SmartSyncOffice', '欢迎您使用SmartSyncOffice智能办公系统！', 'SYSTEM', NULL, NULL, 0, NOW()),
(1, '系统升级通知', '系统将于今晚22:00进行例行维护，请提前保存工作。', 'SYSTEM', NULL, NULL, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, '您有新的审批待处理', '您有一条请假审批需要处理，请及时查看。', 'APPROVAL', 'LEAVE', 1, 0, NOW()),
(1, '文件上传成功', '您的文件「2024年度报告.pdf」已上传成功。', 'FILE', NULL, NULL, 0, NOW());

INSERT INTO sys_announcement (title, content, priority, status, publish_time, create_by, create_time) VALUES
('系统上线公告', 'SmartSyncOffice智能办公系统正式上线！', 2, 1, NOW(), 1, NOW()),
('春节放假通知', '2024年春节放假安排：2月9日至2月17日放假调休，共9天。', 1, 1, DATE_SUB(NOW(), INTERVAL 3 DAY), 1, DATE_SUB(NOW(), INTERVAL 3 DAY));
