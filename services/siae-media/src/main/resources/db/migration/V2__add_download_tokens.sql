-- 添加下载令牌表
-- 用于支持单次使用和IP绑定的下载签名

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
