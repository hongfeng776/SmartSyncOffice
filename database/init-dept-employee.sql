-- 部门表
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '部门ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    ancestors VARCHAR(500) DEFAULT '' COMMENT '祖级列表',
    dept_name VARCHAR(50) NOT NULL COMMENT '部门名称',
    dept_code VARCHAR(50) COMMENT '部门编码',
    sort_order INT DEFAULT 0 COMMENT '排序',
    leader VARCHAR(50) COMMENT '负责人',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态 1:启用 0:禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 员工表（独立于系统用户表，用于更详细的员工信息）
DROP TABLE IF EXISTS sys_employee;
CREATE TABLE sys_employee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '员工ID',
    emp_no VARCHAR(50) NOT NULL UNIQUE COMMENT '员工编号',
    user_id BIGINT COMMENT '关联用户ID',
    dept_id BIGINT NOT NULL COMMENT '部门ID',
    real_name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender TINYINT DEFAULT 0 COMMENT '性别 0:未知 1:男 2:女',
    birthday DATE COMMENT '出生日期',
    id_card VARCHAR(18) COMMENT '身份证号',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    position VARCHAR(50) COMMENT '职位',
    entry_date DATE COMMENT '入职日期',
    work_place VARCHAR(100) COMMENT '工作地点',
    avatar VARCHAR(255) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态 1:在职 0:离职',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
    INDEX idx_dept_id (dept_id),
    INDEX idx_user_id (user_id),
    INDEX idx_emp_no (emp_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工信息表';

-- 插入默认部门数据
INSERT INTO sys_dept (id, parent_id, ancestors, dept_name, dept_code, sort_order, leader, phone, email, status) VALUES
(1, 0, '0', '总公司', 'HQ', 0, '张三', '13800000000', 'hq@example.com', 1),
(2, 1, '0,1', '技术部', 'TECH', 1, '李四', '13800000001', 'tech@example.com', 1),
(3, 1, '0,1', '市场部', 'MARKET', 2, '王五', '13800000002', 'market@example.com', 1),
(4, 1, '0,1', '人事部', 'HR', 3, '赵六', '13800000003', 'hr@example.com', 1),
(5, 2, '0,1,2', '前端组', 'FRONTEND', 1, '钱七', '13800000010', 'frontend@example.com', 1),
(6, 2, '0,1,2', '后端组', 'BACKEND', 2, '孙八', '13800000011', 'backend@example.com', 1);

-- 插入默认员工数据
INSERT INTO sys_employee (emp_no, user_id, dept_id, real_name, gender, phone, email, position, entry_date, status) VALUES
('EMP001', 1, 1, '系统管理员', 1, '13800000001', 'admin@example.com', 'CEO', '2020-01-01', 1),
('EMP002', 2, 5, '普通员工', 2, '13800000002', 'employee@example.com', '前端开发', '2021-06-01', 1);
