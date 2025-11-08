-- 添加生命周期策略表

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

-- 插入默认策略示例
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
