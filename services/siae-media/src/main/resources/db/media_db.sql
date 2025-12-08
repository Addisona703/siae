-- =============================================================================
-- SIAE Media Service - 媒体服务数据库初始化脚本（基础版）
-- 版本: 2.0
-- 描述: 简化的媒体服务数据库表结构
-- 创建日期: 2025-11-26
-- 主要功能:
--   1. 文件管理（上传、存储、访问策略）
--   2. 分片上传支持
--   3. 审计日志和操作追踪
-- 变更说明:
--   - 简化为 4 张核心表：files、uploads、multipart_parts、audit_logs
--   - 添加 access_policy 字段（PUBLIC/PRIVATE）
--   - 添加 filename 字段
--   - 移除不需要的表和字段
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
    `filename` VARCHAR(255) NOT NULL COMMENT '文件名',
    `bucket` VARCHAR(128) NOT NULL COMMENT '存储桶名称',
    `storage_key` VARCHAR(512) NOT NULL COMMENT '对象存储键',
    `size` BIGINT NOT NULL COMMENT '文件大小（字节）',
    `mime` VARCHAR(128) COMMENT 'MIME类型',
    `sha256` CHAR(64) COMMENT 'SHA256校验和',
    `access_policy` VARCHAR(20) NOT NULL DEFAULT 'PRIVATE' COMMENT '访问策略：PUBLIC/PRIVATE',
    `status` VARCHAR(16) NOT NULL DEFAULT 'init' COMMENT '状态: init/uploading/completed/failed',
    `biz_tags` JSON COMMENT '业务标签',
    `ext` JSON COMMENT '扩展属性',
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `deleted_at` DATETIME(3) NULL DEFAULT NULL COMMENT '删除时间（软删除）',
    INDEX `idx_files_tenant_status` (`tenant_id`, `status`),
    INDEX `idx_files_owner` (`owner_id`),
    INDEX `idx_files_access_policy` (`access_policy`),
    INDEX `idx_files_tenant_policy` (`tenant_id`, `access_policy`),
    INDEX `idx_files_sha256` (`sha256`),
    INDEX `idx_files_created_at` (`created_at`),
    INDEX `idx_files_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件实体表';

-- ----------------------------
-- 2. uploads 表 - 上传会话表
-- ----------------------------
CREATE TABLE `uploads` (
    `upload_id` VARCHAR(200) PRIMARY KEY COMMENT '上传会话ID (S3 Multipart Upload ID)',
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
    `upload_id` VARCHAR(200) NOT NULL COMMENT '上传会话ID',
    `part_number` INT NOT NULL COMMENT '分片编号（从1开始）',
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
    `action` ENUM('init', 'complete', 'download', 'delete', 'update_policy') NOT NULL COMMENT '操作类型',
    `ip` VARCHAR(45) COMMENT 'IP地址',
    `user_agent` TEXT COMMENT '用户代理',
    `metadata` JSON COMMENT '元数据',
    `occurred_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '发生时间',
    INDEX `idx_audit_file` (`file_id`),
    INDEX `idx_audit_tenant_action_time` (`tenant_id`, `action`, `occurred_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';

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
