-- SmartSyncOffice 数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS smart_sync_office DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE smart_sync_office;

-- 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(200) COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态 1:启用 0:禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '权限ID',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    resource_type VARCHAR(20) DEFAULT 'menu' COMMENT '资源类型 menu:菜单 button:按钮',
    parent_id BIGINT DEFAULT 0 COMMENT '父级权限ID',
    path VARCHAR(200) COMMENT '路由路径',
    component VARCHAR(200) COMMENT '组件路径',
    icon VARCHAR(50) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 1:启用 0:禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像URL',
    dept_id BIGINT COMMENT '部门ID',
    status TINYINT DEFAULT 1 COMMENT '状态 1:启用 0:禁用',
    last_login_time DATETIME COMMENT '最后登录时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 用户角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 插入默认数据
-- 角色
INSERT INTO sys_role (id, role_name, role_code, description) VALUES
(1, '超级管理员', 'ADMIN', '系统最高权限管理员'),
(2, '普通员工', 'EMPLOYEE', '普通员工权限');

-- 权限
INSERT INTO sys_permission (id, permission_name, permission_code, resource_type, parent_id, path, component, icon, sort_order) VALUES
(1, '系统首页', 'dashboard', 'menu', 0, '/dashboard', 'dashboard/index', 'HomeFilled', 1),
(2, '组织架构', 'org', 'menu', 0, '/org', '', 'OfficeBuilding', 2),
(3, '部门管理', 'org:dept', 'menu', 2, '/org/dept', 'org/dept/index', 'Menu', 1),
(4, '员工列表', 'org:employee', 'menu', 2, '/org/employee', 'org/employee/index', 'User', 2),
(5, '系统管理', 'system', 'menu', 0, '/system', '', 'Setting', 3),
(6, '用户管理', 'system:user', 'menu', 5, '/system/user', 'system/user/index', 'User', 1),
(7, '角色管理', 'system:role', 'menu', 5, '/system/role', 'system/role/index', 'UserFilled', 2),
(8, '权限管理', 'system:permission', 'menu', 5, '/system/permission', 'system/permission/index', 'Lock', 3),
(9, '个人中心', 'profile', 'menu', 0, '/profile', 'profile/index', 'User', 99),
(10, '待办事项', 'todo', 'menu', 0, '/todo', 'todo/index', 'TodoList', 4),
(11, '文件管理', 'file', 'menu', 0, '/file', 'file/index', 'Folder', 5);

-- 默认用户 (启动后端时会自动创建并设置密码为 123456)
INSERT INTO sys_user (id, username, password, real_name, email, phone) VALUES
(1, 'admin', 'placeholder', '系统管理员', 'admin@example.com', '13800000001'),
(2, 'employee', 'placeholder', '普通员工', 'employee@example.com', '13800000002');

-- 用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2);

-- 角色权限关联 (管理员拥有所有权限)
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11),
(2, 1), (2, 9), (2, 10), (2, 11);

-- 登录日志表
DROP TABLE IF EXISTS sys_login_log;
CREATE TABLE sys_login_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    real_name VARCHAR(50) COMMENT '真实姓名',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    ip_location VARCHAR(100) COMMENT 'IP归属地',
    browser VARCHAR(100) COMMENT '浏览器',
    operating_system VARCHAR(100) COMMENT '操作系统',
    device_type VARCHAR(20) COMMENT '设备类型',
    login_status TINYINT DEFAULT 1 COMMENT '登录状态 1:成功 0:失败',
    failure_reason VARCHAR(200) COMMENT '失败原因',
    login_time DATETIME COMMENT '登录时间',
    token VARCHAR(255) COMMENT 'Token(截取前50位)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_username (username),
    INDEX idx_login_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';
