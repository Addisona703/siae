-- =============================================================================
-- SIAE Media Service - 媒体服务数据库初始化脚本
-- 版本: 1.0
-- 描述: 完整的媒体服务数据库表结构
-- 创建日期: 2025-11-05
-- 主要功能:
--   1. 文件管理（上传、存储、版本控制）
--   2. 下载签名和令牌管理
--   3. 审计日志和操作追踪
--   4. 配额管理和限制
--   5. 异步处理任务队列
--   6. 生命周期策略管理
-- =============================================================================

-- ----------------------------
-- 初始化数据库
-- ----------------------------
DROP DATABASE IF EXISTS media_db;
CREATE DATABASE IF NOT EXISTS media_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE media_db;

-- 设置SQL模式和字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================================================
-- 核心表结构
-- =============================================================================

-- ----------------------------
-- 1. files 表 - 文件实体表
-- ----------------------------
CREATE TABLE `files` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '文件ID (UUID)',
    `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户ID',
    `owner_id` VARCHAR(64) COMMENT '所有者ID',
    `bucket` VARCHAR(128) NOT NULL COMMENT '存储桶名称',
    `storage_key` VARCHAR(512) NOT NULL COMMENT '对象存储键',
    `size` BIGINT NOT NULL COMMENT '文件大小（字节）',
    `mime` VARCHAR(128) COMMENT 'MIME类型',
    `sha256` CHAR(64) COMMENT 'SHA256校验和',
    `status` VARCHAR(16) NOT NULL DEFAULT 'init' COMMENT '状态: init/uploading/completed/failed/deleted',
    `acl` JSON COMMENT '访问控制列表',
    `biz_tags` JSON COMMENT '业务标签',
    `ext` JSON COMMENT '扩展属性',
    `checksum` JSON COMMENT '校验和信息',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `deleted_at` DATETIME(3) NULL DEFAULT NULL COMMENT '删除时间（软删除）',
    INDEX `idx_files_tenant_status` (`tenant_id`, `status`),
    INDEX `idx_files_owner` (`owner_id`),
    INDEX `idx_files_sha256` (`sha256`),
    INDEX `idx_files_created_at` (`created_at`),
    INDEX `idx_files_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件实体表';

-- ----------------------------
-- 2. uploads 表 - 上传会话表
-- ----------------------------
CREATE TABLE `uploads` (
    `upload_id` VARCHAR(36) PRIMARY KEY COMMENT '上传会话ID (UUID)',
    `file_id` VARCHAR(36) NOT NULL COMMENT '关联的文件ID',
    `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户ID',
    `multipart` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否分片上传',
    `part_size` INT COMMENT '分片大小（字节）',
    `total_parts` INT COMMENT '总分片数',
    `completed_parts` INT DEFAULT 0 COMMENT '已完成分片数',
    `expire_at` DATETIME(3) NOT NULL COMMENT '过期时间',
    `status` VARCHAR(16) NOT NULL DEFAULT 'init' COMMENT '状态: init/in_progress/completed/expired/aborted',
    `callbacks` JSON COMMENT '回调配置',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    INDEX `idx_uploads_file_id` (`file_id`),
    INDEX `idx_uploads_tenant_status` (`tenant_id`, `status`),
    INDEX `idx_uploads_expire_at` (`expire_at`),
    FOREIGN KEY (`file_id`) REFERENCES `files`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='上传会话表';

-- ----------------------------
-- 3. multipart_parts 表 - 分片上传记录表
-- ----------------------------
CREATE TABLE `multipart_parts` (
    `upload_id` VARCHAR(36) NOT NULL COMMENT '上传会话ID',
    `part_number` INT NOT NULL COMMENT '分片编号',
    `etag` VARCHAR(256) COMMENT 'ETag标识',
    `size` BIGINT COMMENT '分片大小（字节）',
    `checksum` CHAR(64) COMMENT '分片校验和',
    `uploaded_at` DATETIME(3) COMMENT '上传完成时间',
    PRIMARY KEY (`upload_id`, `part_number`),
    INDEX `idx_parts_uploaded_at` (`uploaded_at`),
    FOREIGN KEY (`upload_id`) REFERENCES `uploads`(`upload_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分片上传记录表';

-- ----------------------------
-- 4. audit_logs 表 - 审计日志表
-- ----------------------------
CREATE TABLE `audit_logs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    `file_id` VARCHAR(36) COMMENT '文件ID',
    `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户ID',
    `actor_type` ENUM('service', 'user', 'system') NOT NULL COMMENT '操作者类型',
    `actor_id` VARCHAR(64) NOT NULL COMMENT '操作者ID',
    `action` ENUM('init', 'complete', 'sign', 'download', 'delete', 'restore', 'update_acl', 'generate_preview') NOT NULL COMMENT '操作类型',
    `ip` VARCHAR(45) COMMENT 'IP地址',
    `user_agent` TEXT COMMENT '用户代理',
    `metadata` JSON COMMENT '元数据',
    `occurred_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '发生时间',
    INDEX `idx_audit_file` (`file_id`),
    INDEX `idx_audit_tenant_action_time` (`tenant_id`, `action`, `occurred_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';

-- ----------------------------
-- 5. processing_jobs 表 - 异步处理任务表
-- ----------------------------
CREATE TABLE `processing_jobs` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '任务ID (UUID)',
    `file_id` VARCHAR(36) NOT NULL COMMENT '文件ID',
    `job_type` ENUM('scan', 'thumb', 'ocr', 'transcode', 'preview', 'lifecycle', 'notify') NOT NULL COMMENT '任务类型',
    `status` ENUM('pending', 'running', 'success', 'failed', 'dead_letter') NOT NULL DEFAULT 'pending' COMMENT '任务状态',
    `priority` TINYINT DEFAULT 5 COMMENT '优先级 (1-10)',
    `attempts` INT DEFAULT 0 COMMENT '重试次数',
    `last_error` TEXT COMMENT '最后错误信息',
    `payload` JSON COMMENT '任务载荷',
    `scheduled_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '调度时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    INDEX `idx_jobs_status_type` (`status`, `job_type`),
    INDEX `idx_jobs_file_type` (`file_id`, `job_type`),
    INDEX `idx_jobs_scheduled` (`scheduled_at`),
    FOREIGN KEY (`file_id`) REFERENCES `files`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='异步处理任务表';

-- ----------------------------
-- 6. quotas 表 - 租户配额表
-- ----------------------------
CREATE TABLE `quotas` (
    `tenant_id` VARCHAR(64) PRIMARY KEY COMMENT '租户ID',
    `bytes_used` BIGINT NOT NULL DEFAULT 0 COMMENT '已使用存储（字节）',
    `objects_count` BIGINT NOT NULL DEFAULT 0 COMMENT '对象数量',
    `limits` JSON NOT NULL COMMENT '配额限制',
    `reset_strategy` ENUM('monthly', 'rolling') DEFAULT 'monthly' COMMENT '重置策略',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户配额表';

-- ----------------------------
-- 7. file_versions 表 - 文件版本表
-- ----------------------------
CREATE TABLE `file_versions` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '版本ID (UUID)',
    `file_id` VARCHAR(36) NOT NULL COMMENT '文件ID',
    `version` INT NOT NULL COMMENT '版本号',
    `storage_key` VARCHAR(512) NOT NULL COMMENT '对象存储键',
    `size` BIGINT NOT NULL COMMENT '文件大小（字节）',
    `sha256` CHAR(64) COMMENT 'SHA256校验和',
    `ext` JSON COMMENT '扩展属性',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    INDEX `idx_versions_file_version` (`file_id`, `version`),
    FOREIGN KEY (`file_id`) REFERENCES `files`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件版本表';

-- ----------------------------
-- 8. file_derivatives 表 - 文件衍生物表
-- ----------------------------
CREATE TABLE `file_derivatives` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '衍生物ID (UUID)',
    `file_id` VARCHAR(36) NOT NULL COMMENT '源文件ID',
    `type` ENUM('thumb', 'preview', 'transcode') NOT NULL COMMENT '衍生物类型',
    `storage_key` VARCHAR(512) NOT NULL COMMENT '对象存储键',
    `size` BIGINT NOT NULL COMMENT '文件大小（字节）',
    `metadata` JSON COMMENT '元数据',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    INDEX `idx_derivatives_file_type` (`file_id`, `type`),
    FOREIGN KEY (`file_id`) REFERENCES `files`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件衍生物表';

-- ----------------------------
-- 9. download_tokens 表 - 下载令牌表
-- ----------------------------
CREATE TABLE `download_tokens` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '令牌ID (UUID)',
    `file_id` VARCHAR(36) NOT NULL COMMENT '文件ID',
    `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `token` VARCHAR(64) NOT NULL COMMENT '令牌字符串',
    `bind_ip` VARCHAR(45) COMMENT '绑定的IP地址',
    `single_use` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否单次使用',
    `used` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已使用',
    `expires_at` DATETIME(3) NOT NULL COMMENT '过期时间',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `used_at` DATETIME(3) NULL DEFAULT NULL COMMENT '使用时间',
    UNIQUE INDEX `idx_token` (`token`),
    INDEX `idx_download_tokens_file` (`file_id`),
    INDEX `idx_download_tokens_tenant` (`tenant_id`),
    INDEX `idx_download_tokens_expires` (`expires_at`),
    FOREIGN KEY (`file_id`) REFERENCES `files`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='下载令牌表';

-- ----------------------------
-- 10. lifecycle_policies 表 - 生命周期策略表
-- ----------------------------
CREATE TABLE `lifecycle_policies` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '策略ID (UUID)',
    `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户ID',
    `name` VARCHAR(128) NOT NULL COMMENT '策略名称',
    `description` TEXT COMMENT '策略描述',
    `enabled` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    `rules` JSON NOT NULL COMMENT '策略规则',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    INDEX `idx_lifecycle_tenant` (`tenant_id`),
    INDEX `idx_lifecycle_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='生命周期策略表';

-- =============================================================================
-- 初始化数据
-- =============================================================================

-- ----------------------------
-- 初始化默认配额
-- ----------------------------
INSERT INTO `quotas` (`tenant_id`, `bytes_used`, `objects_count`, `limits`, `reset_strategy`)
VALUES ('default', 0, 0, '{"max_bytes": 10737418240, "max_objects": 10000, "daily_download": "unlimited"}', 'monthly');

-- ----------------------------
-- 初始化默认生命周期策略
-- ----------------------------
INSERT INTO `lifecycle_policies` (`id`, `tenant_id`, `name`, `description`, `enabled`, `rules`)
VALUES (
    UUID(),
    'default',
    '默认生命周期策略',
    '归档90天前的文件，删除180天前的文件，清理30天前的已删除文件',
    FALSE,
    JSON_OBJECT(
        'archiveAfterDays', 90,
        'deleteAfterDays', 180,
        'cleanupDeletedAfterDays', 30
    )
);

-- =============================================================================
-- 完成初始化
-- =============================================================================
SET FOREIGN_KEY_CHECKS = 1;

-- 显示所有表
SHOW TABLES;

-- 显示表结构统计
SELECT 
    TABLE_NAME as '表名',
    TABLE_COMMENT as '说明',
    TABLE_ROWS as '预估行数',
    ROUND(DATA_LENGTH/1024/1024, 2) as '数据大小(MB)',
    ROUND(INDEX_LENGTH/1024/1024, 2) as '索引大小(MB)'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'media_db'
ORDER BY TABLE_NAME;
