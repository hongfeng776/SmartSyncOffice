-- 文件文件夹表
CREATE TABLE IF NOT EXISTS `sys_file_folder` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `folder_name` VARCHAR(255) NOT NULL COMMENT '文件夹名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父文件夹ID，0表示根目录',
    `folder_path` VARCHAR(500) DEFAULT NULL COMMENT '文件夹完整路径',
    `level` INT DEFAULT 1 COMMENT '文件夹层级',
    `sort` INT DEFAULT 0 COMMENT '排序号',
    `creator_id` VARCHAR(50) DEFAULT NULL COMMENT '创建者ID',
    `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建者姓名',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件文件夹表';

-- 插入默认文件夹
INSERT INTO `sys_file_folder` (`folder_name`, `parent_id`, `folder_path`, `level`, `sort`, `creator_id`, `creator_name`) VALUES
('图片', 0, '/图片', 1, 1, '1', 'admin'),
('文档', 0, '/文档', 1, 2, '1', 'admin'),
('视频', 0, '/视频', 1, 3, '1', 'admin'),
('其他', 0, '/其他', 1, 4, '1', 'admin');

-- 文件管理表
CREATE TABLE IF NOT EXISTS `sys_file` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `original_filename` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `stored_filename` VARCHAR(255) NOT NULL COMMENT '存储文件名',
    `file_path` VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    `file_size` VARCHAR(50) DEFAULT NULL COMMENT '文件大小',
    `file_type` VARCHAR(20) DEFAULT NULL COMMENT '文件类型(image/document/video/audio/other)',
    `content_type` VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型',
    `file_extension` VARCHAR(20) DEFAULT NULL COMMENT '文件扩展名',
    `bucket_name` VARCHAR(100) DEFAULT NULL COMMENT '存储桶名称',
    `folder_id` BIGINT DEFAULT NULL COMMENT '文件夹ID',
    `uploader_id` VARCHAR(50) DEFAULT NULL COMMENT '上传者ID',
    `uploader_name` VARCHAR(100) DEFAULT NULL COMMENT '上传者姓名',
    `download_count` INT DEFAULT 0 COMMENT '下载次数',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_folder_id` (`folder_id`),
    KEY `idx_uploader_id` (`uploader_id`),
    KEY `idx_file_type` (`file_type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件管理表';

-- 文件上传日志表
CREATE TABLE IF NOT EXISTS `sys_file_upload_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `original_filename` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_size` BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    `file_extension` VARCHAR(20) DEFAULT NULL COMMENT '文件扩展名',
    `content_type` VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型',
    `folder_id` BIGINT DEFAULT NULL COMMENT '文件夹ID',
    `uploader_id` VARCHAR(50) DEFAULT NULL COMMENT '上传者ID',
    `uploader_name` VARCHAR(100) DEFAULT NULL COMMENT '上传者姓名',
    `uploader_ip` VARCHAR(50) DEFAULT NULL COMMENT '上传者IP',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '浏览器UA',
    `upload_status` VARCHAR(20) DEFAULT NULL COMMENT '上传状态(UPLOADING/SUCCESS/FAILED/DELETED)',
    `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
    `stored_filename` VARCHAR(255) DEFAULT NULL COMMENT '存储文件名',
    `file_path` VARCHAR(500) DEFAULT NULL COMMENT '文件存储路径',
    `bucket_name` VARCHAR(100) DEFAULT NULL COMMENT '存储桶名称',
    `file_id` BIGINT DEFAULT NULL COMMENT '关联的文件ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_uploader_id` (`uploader_id`),
    KEY `idx_upload_status` (`upload_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件上传日志表';

-- 分片上传记录表
CREATE TABLE IF NOT EXISTS `sys_file_upload` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `upload_id` VARCHAR(64) NOT NULL COMMENT '上传任务ID',
    `filename` VARCHAR(255) NOT NULL COMMENT '文件名',
    `total_size` BIGINT DEFAULT 0 COMMENT '文件总大小(字节)',
    `total_chunks` INT DEFAULT 0 COMMENT '总分片数',
    `uploaded_chunks` INT DEFAULT 0 COMMENT '已上传分片数',
    `uploaded_chunk_numbers` TEXT DEFAULT NULL COMMENT '已上传分片号(逗号分隔)',
    `folder_id` BIGINT DEFAULT NULL COMMENT '文件夹ID',
    `uploader_id` VARCHAR(50) DEFAULT NULL COMMENT '上传者ID',
    `uploader_name` VARCHAR(100) DEFAULT NULL COMMENT '上传者姓名',
    `status` VARCHAR(20) DEFAULT NULL COMMENT '状态(UPLOADING/COMPLETED/FAILED)',
    `stored_path` VARCHAR(500) DEFAULT NULL COMMENT '存储路径',
    `md5` VARCHAR(64) DEFAULT NULL COMMENT '文件MD5',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_upload_id` (`upload_id`),
    KEY `idx_uploader_id` (`uploader_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分片上传记录表';
