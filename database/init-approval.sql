-- 审批相关表初始化

USE smart_sync_office;

-- 审批类型表
DROP TABLE IF EXISTS approval_type;
CREATE TABLE approval_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '审批类型ID',
    type_code VARCHAR(50) NOT NULL UNIQUE COMMENT '审批类型编码',
    type_name VARCHAR(50) NOT NULL COMMENT '审批类型名称',
    description VARCHAR(200) COMMENT '描述',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 1:启用 0:禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批类型表';

-- 审批主表
DROP TABLE IF EXISTS approval;
CREATE TABLE approval (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '审批ID',
    approval_no VARCHAR(50) NOT NULL UNIQUE COMMENT '审批编号',
    approval_type VARCHAR(50) NOT NULL COMMENT '审批类型',
    title VARCHAR(200) NOT NULL COMMENT '审批标题',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    applicant_name VARCHAR(50) COMMENT '申请人姓名',
    dept_id BIGINT COMMENT '部门ID',
    dept_name VARCHAR(100) COMMENT '部门名称',
    approver_id BIGINT COMMENT '审批人ID',
    approver_name VARCHAR(50) COMMENT '审批人姓名',
    status TINYINT DEFAULT 0 COMMENT '状态 0:待提交 1:待审批 2:已通过 3:已驳回 4:已撤销',
    current_node VARCHAR(50) COMMENT '当前审批节点',
    remark VARCHAR(500) COMMENT '备注',
    submit_time DATETIME COMMENT '提交时间',
    approve_time DATETIME COMMENT '审批时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
    INDEX idx_applicant_id (applicant_id),
    INDEX idx_approver_id (approver_id),
    INDEX idx_status (status),
    INDEX idx_approval_type (approval_type),
    INDEX idx_submit_time (submit_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批主表';

-- 请假申请表
DROP TABLE IF EXISTS approval_leave;
CREATE TABLE approval_leave (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    approval_id BIGINT NOT NULL COMMENT '审批ID',
    leave_type VARCHAR(50) NOT NULL COMMENT '请假类型',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    leave_days DECIMAL(5,1) NOT NULL COMMENT '请假天数',
    reason VARCHAR(500) NOT NULL COMMENT '请假原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
    INDEX idx_approval_id (approval_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假申请表';

-- 加班申请表
DROP TABLE IF EXISTS approval_overtime;
CREATE TABLE approval_overtime (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    approval_id BIGINT NOT NULL COMMENT '审批ID',
    overtime_type VARCHAR(50) NOT NULL COMMENT '加班类型',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    overtime_hours DECIMAL(5,1) NOT NULL COMMENT '加班时长(小时)',
    reason VARCHAR(500) NOT NULL COMMENT '加班原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
    INDEX idx_approval_id (approval_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='加班申请表';

-- 报销申请表
DROP TABLE IF EXISTS approval_reimbursement;
CREATE TABLE approval_reimbursement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    approval_id BIGINT NOT NULL COMMENT '审批ID',
    expense_type VARCHAR(50) NOT NULL COMMENT '费用类型',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '总金额',
    expense_date DATE NOT NULL COMMENT '费用日期',
    reason VARCHAR(500) NOT NULL COMMENT '费用说明',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
    INDEX idx_approval_id (approval_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报销申请表';

-- 审批记录表
DROP TABLE IF EXISTS approval_record;
CREATE TABLE approval_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    approval_id BIGINT NOT NULL COMMENT '审批ID',
    node VARCHAR(50) COMMENT '审批节点',
    approver_id BIGINT COMMENT '审批人ID',
    approver_name VARCHAR(50) COMMENT '审批人姓名',
    action VARCHAR(20) NOT NULL COMMENT '操作 SUBMIT:提交 APPROVE:通过 REJECT:驳回 CANCEL:撤销',
    opinion VARCHAR(500) COMMENT '审批意见',
    operate_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
    INDEX idx_approval_id (approval_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批记录表';

-- 插入审批类型数据
INSERT INTO approval_type (type_code, type_name, description, sort_order) VALUES
('LEAVE', '请假审批', '员工请假申请审批', 1),
('OVERTIME', '加班审批', '员工加班申请审批', 2),
('REIMBURSEMENT', '报销审批', '员工费用报销申请审批', 3);

-- 给普通员工添加审批管理权限
INSERT INTO sys_permission (id, permission_name, permission_code, resource_type, parent_id, path, component, icon, sort_order) VALUES
(12, '流程审批', 'approval', 'menu', 0, '/approval', '', 'Document', 6),
(13, '发起审批', 'approval:create', 'menu', 12, '/approval/create', 'approval/create/index', 'Plus', 1),
(14, '待我审批', 'approval:todo', 'menu', 12, '/approval/todo', 'approval/todo/index', 'Clock', 2),
(15, '我发起的', 'approval:my', 'menu', 12, '/approval/my', 'approval/my/index', 'Tickets', 3),
(16, '审批详情', 'approval:detail', 'menu', 12, '/approval/detail', 'approval/detail/index', 'View', 4);

-- 消息通知表
DROP TABLE IF EXISTS sys_notification;
CREATE TABLE sys_notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '通知ID',
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    type VARCHAR(50) NOT NULL COMMENT '通知类型',
    business_type VARCHAR(50) COMMENT '业务类型',
    business_id BIGINT COMMENT '业务ID',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读 0:未读 1:已读',
    read_time DATETIME COMMENT '阅读时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- 给普通员工角色添加审批权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(2, 12), (2, 13), (2, 14), (2, 15), (2, 16);
