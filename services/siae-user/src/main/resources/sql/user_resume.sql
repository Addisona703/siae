-- =====================================================
-- user_resume - 用户简历表
-- 描述: 存储用户的简历信息，每个用户只能有一份简历
-- 复杂字段（期望职位、工作经历、项目经验、教育经历、获奖情况）使用 JSON 格式存储
-- =====================================================
USE user_db;

CREATE TABLE IF NOT EXISTS `user_resume` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID，关联user表',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `gender` VARCHAR(10) DEFAULT '男' COMMENT '性别',
    `age` INT DEFAULT NULL COMMENT '年龄',
    `work_status` VARCHAR(50) DEFAULT NULL COMMENT '工作状态',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `wechat` VARCHAR(50) DEFAULT NULL COMMENT '微信号',
    `job_status` VARCHAR(50) DEFAULT NULL COMMENT '求职状态',
    `graduation_year` VARCHAR(20) DEFAULT NULL COMMENT '毕业年份',
    `expected_jobs` JSON DEFAULT NULL COMMENT '期望职位列表（JSON格式）',
    `advantages` TEXT DEFAULT NULL COMMENT '个人优势',
    `work_experience` JSON DEFAULT NULL COMMENT '工作经历列表（JSON格式）',
    `projects` JSON DEFAULT NULL COMMENT '项目经验列表（JSON格式）',
    `education` JSON DEFAULT NULL COMMENT '教育经历列表（JSON格式）',
    `awards` JSON DEFAULT NULL COMMENT '获奖情况列表（JSON格式）',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否逻辑删除：0否，1是',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户简历表';
