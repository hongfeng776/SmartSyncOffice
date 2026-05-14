-- 任务相关表初始化

USE smart_sync_office;

-- 任务主表
DROP TABLE IF EXISTS task;
CREATE TABLE task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
    task_no VARCHAR(50) NOT NULL UNIQUE COMMENT '任务编号',
    title VARCHAR(200) NOT NULL COMMENT '任务标题',
    description TEXT COMMENT '任务描述',
    priority TINYINT DEFAULT 1 COMMENT '优先级 1:低 2:中 3:高',
    task_type VARCHAR(50) COMMENT '任务类型',
    creator_id BIGINT NOT NULL COMMENT '创建人ID',
    creator_name VARCHAR(50) COMMENT '创建人姓名',
    assignee_id BIGINT COMMENT '执行人ID',
    assignee_name VARCHAR(50) COMMENT '执行人姓名',
    dept_id BIGINT COMMENT '部门ID',
    dept_name VARCHAR(100) COMMENT '部门名称',
    status TINYINT DEFAULT 0 COMMENT '状态 0:待分配 1:待执行 2:进行中 3:已完成 4:已取消',
    progress INT DEFAULT 0 COMMENT '任务进度 0-100',
    deadline DATETIME COMMENT '截止时间',
    start_time DATETIME COMMENT '开始时间',
    complete_time DATETIME COMMENT '完成时间',
    remark VARCHAR(500) COMMENT '备注',
    remind_sent TINYINT DEFAULT 0 COMMENT '临近截止提醒是否已发送 0:未发送 1:已发送',
    overdue_remind_sent TINYINT DEFAULT 0 COMMENT '逾期提醒是否已发送 0:未发送 1:已发送',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
    INDEX idx_creator_id (creator_id),
    INDEX idx_assignee_id (assignee_id),
    INDEX idx_status (status),
    INDEX idx_deadline (deadline),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务主表';

-- 任务记录表
DROP TABLE IF EXISTS task_record;
CREATE TABLE task_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    action VARCHAR(50) NOT NULL COMMENT '操作 CREATE:创建 ASSIGN:分配 START:开始 UPDATE:更新 COMPLETE:完成 CANCEL:取消',
    old_status TINYINT COMMENT '原状态',
    new_status TINYINT COMMENT '新状态',
    comment VARCHAR(500) COMMENT '备注说明',
    progress INT COMMENT '进度变化',
    operate_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
    INDEX idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务记录表';

-- 给普通员工添加任务管理权限
INSERT INTO sys_permission (id, permission_name, permission_code, resource_type, parent_id, path, component, icon, sort_order) VALUES
(17, '任务管理', 'task', 'menu', 0, '/task', '', 'List', 7),
(18, '发布任务', 'task:create', 'menu', 17, '/task/create', 'task/create/index', 'Plus', 1),
(19, '我的任务', 'task:my', 'menu', 17, '/task/my', 'task/my/index', 'Tickets', 2),
(20, '任务进度', 'task:progress', 'menu', 17, '/task/progress', 'task/progress/index', 'DataLine', 3),
(21, '任务详情', 'task:detail', 'menu', 17, '/task/detail', 'task/detail/index', 'View', 4);

-- 给普通员工角色添加任务权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(2, 17), (2, 18), (2, 19), (2, 20), (2, 21);
