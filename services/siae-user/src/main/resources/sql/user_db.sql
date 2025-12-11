-- =============================================================================
-- SIAE (软件协会官网) - 用户数据库 (user_db)
-- 版本: 5.0 (重构版, fixed & formatted)
-- 描述:
--   1. 合并 member 和 member_candidate 为 membership 表
--   2. member_department 和 member_position 绑定 membership_id
--   3. 简化 major_class_enrollment 的 member_type 和 status
--   4. user 表添加学号字段
-- 修改日期: 2025-11-09
-- =============================================================================

DROP DATABASE IF EXISTS `user_db`;
CREATE DATABASE IF NOT EXISTS `user_db`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE `user_db`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 数据字典表 (Lookup Tables)
-- ----------------------------
CREATE TABLE `major` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(64) NOT NULL COMMENT '专业名称',
    `code` VARCHAR(32) NOT NULL COMMENT '专业编码',
    `abbr` VARCHAR(16) COMMENT '专业简称',
    `college_name` VARCHAR(64) NOT NULL COMMENT '所属学院名称',
    UNIQUE KEY `uk_major_name` (`name`),
    UNIQUE KEY `uk_major_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专业字典表';

CREATE TABLE `department` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(64) NOT NULL COMMENT '部门名称',
    UNIQUE KEY `uk_department_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门字典表';

CREATE TABLE `position` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(64) NOT NULL COMMENT '职位名称',
    UNIQUE KEY `uk_position_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='职位字典表';

CREATE TABLE `award_level` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(64) NOT NULL COMMENT '奖项等级名称',
    `order_id` INT NOT NULL DEFAULT 0 COMMENT '排序ID，值越小排序越靠前',
    UNIQUE KEY `uk_award_level_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='奖项等级字典表';

CREATE TABLE `award_type` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(64) NOT NULL COMMENT '奖项类型名称',
    `order_id` INT NOT NULL DEFAULT 0 COMMENT '排序ID，值越小排序越靠前',
    UNIQUE KEY `uk_award_type_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='奖项类型字典表';

-- ----------------------------
-- 2. 核心用户与业务表
-- ----------------------------
CREATE TABLE `user` (
    `id` BIGINT UNSIGNED PRIMARY KEY COMMENT '主键，使用雪花ID生成',
    `username` VARCHAR(64) NOT NULL COMMENT '登录名/用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '加密密码 (请使用BCrypt)',
    `student_id` VARCHAR(32) DEFAULT NULL COMMENT '学号',
    `avatar_file_id` VARCHAR(36) DEFAULT NULL COMMENT '用户头像文件ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否逻辑删除：0否，1是',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_student_id` (`student_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_is_deleted` (`is_deleted`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户主表';

CREATE TABLE `user_profile` (
    `user_id` BIGINT UNSIGNED PRIMARY KEY COMMENT '外键，关联user表',
    `nickname` VARCHAR(64) COMMENT '昵称',
    `real_name` VARCHAR(64) COMMENT '真实姓名',
    `bio` TEXT COMMENT '个人简介',
    `background_file_id` VARCHAR(36) COMMENT '主页背景文件ID',
    `email` VARCHAR(128) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号码',
    `qq` VARCHAR(20) COMMENT 'QQ号 (用于联系)',
    `wechat` VARCHAR(64) COMMENT '微信号 (用于联系)',
    `id_card` VARCHAR(18) COMMENT '身份证号',
    `gender` TINYINT DEFAULT 0 COMMENT '性别：0未知，1男，2女',
    `birthday` DATE COMMENT '出生日期',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_email` (`email`),
    INDEX `idx_phone` (`phone`),
    INDEX `idx_real_name` (`real_name`),
    INDEX `idx_nickname` (`nickname`),
    CONSTRAINT `fk_profile_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户详情表';

-- 合并后成员表
CREATE TABLE IF NOT EXISTS `membership` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID（唯一）',
    `headshot_file_id` VARCHAR(36) DEFAULT NULL COMMENT '成员大头照文件ID',
    `lifecycle_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0候选,1正式',
    `join_date` DATE NULL COMMENT '成为正式成员的日期（原member.join_date）',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '保留位（一般保持0）',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_membership_user` (`user_id`),
    INDEX `idx_membership_lifecycle` (`lifecycle_status`),
    INDEX `idx_membership_join_date` (`join_date`),
    CONSTRAINT `fk_membership_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成员统一表（候选/正式）';

-- 成员-部门
CREATE TABLE `member_department` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `membership_id` BIGINT UNSIGNED NOT NULL COMMENT '成员ID',
    `department_id` BIGINT UNSIGNED NOT NULL COMMENT '关联部门ID',
    `join_date` DATE NOT NULL COMMENT '加入日期',
    `has_position` TINYINT DEFAULT 0 COMMENT '是否在该部门担任职位：0否，1是',
    UNIQUE KEY `uk_member_department_membership_department` (`membership_id`, `department_id`),
    INDEX `idx_member_department_membership` (`membership_id`),
    INDEX `idx_member_department_department` (`department_id`),
    CONSTRAINT `fk_member_department_membership` FOREIGN KEY (`membership_id`) REFERENCES `membership` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_member_department_department` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成员部门关联表';

-- 成员-职位
CREATE TABLE `member_position` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `membership_id` BIGINT UNSIGNED NOT NULL COMMENT '成员ID',
    `position_id` BIGINT UNSIGNED NOT NULL COMMENT '关联职位ID',
    `department_id` BIGINT UNSIGNED NULL COMMENT '关联部门ID，NULL 表示全协会职位',
    `start_date` DATE NOT NULL COMMENT '任职开始日期',
    `end_date` DATE NULL COMMENT '任职结束日期，NULL 表示仍在任',
    INDEX `idx_member_position_membership` (`membership_id`),
    INDEX `idx_member_position_position` (`position_id`),
    INDEX `idx_member_position_department` (`department_id`),
    INDEX `idx_member_position_active` (`membership_id`, `end_date`),
    CONSTRAINT `fk_member_position_membership` FOREIGN KEY (`membership_id`) REFERENCES `membership` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_member_position_position` FOREIGN KEY (`position_id`) REFERENCES `position` (`id`)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_member_position_department` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成员职位历史表';

-- 获奖
CREATE TABLE `user_award` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键，自增',
    `award_title` VARCHAR(255) NOT NULL COMMENT '奖项名称',
    `award_level_id` BIGINT UNSIGNED NOT NULL COMMENT '关联奖项等级ID',
    `award_type_id` BIGINT UNSIGNED NOT NULL COMMENT '关联奖项类型ID',
    `awarded_by` VARCHAR(255) NOT NULL COMMENT '颁发单位',
    `awarded_at` DATE NOT NULL COMMENT '获奖时间',
    `description` TEXT COMMENT '获奖描述（选填）',
    `certificate_file_id` VARCHAR(36) COMMENT '奖状或证明材料文件ID',
    `team_members` JSON NOT NULL COMMENT '团队成员用户ID数组（包含所有获奖成员）',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0否，1是',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    INDEX `idx_user_award_is_deleted` (`is_deleted`),
    INDEX `idx_user_award_level_id` (`award_level_id`),
    INDEX `idx_user_award_type_id` (`award_type_id`),
    INDEX `idx_user_award_awarded_at` (`awarded_at`),
    CONSTRAINT `fk_award_level` FOREIGN KEY (`award_level_id`) REFERENCES `award_level`(`id`)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_award_type` FOREIGN KEY (`award_type_id`) REFERENCES `award_type`(`id`)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户获奖记录表';

-- 学籍关联（按你的"简化"方案保留 member_type/status 字段）
CREATE TABLE `major_class_enrollment` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `major_id` BIGINT UNSIGNED NOT NULL COMMENT '专业ID',
    `entry_year` INT NOT NULL COMMENT '入学年份',
    `class_no` INT NOT NULL COMMENT '班号',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `member_type` TINYINT DEFAULT 0 COMMENT '成员类型：0非协会成员，1协会成员',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1在读，2离校',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0否，1是',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_major_class_user` (`major_id`, `entry_year`, `class_no`, `user_id`),
    INDEX `idx_major_entry_class` (`major_id`, `entry_year`, `class_no`),
    INDEX `idx_major_class_user_id` (`user_id`),
    INDEX `idx_major_class_status` (`status`),
    INDEX `idx_major_class_is_deleted` (`is_deleted`),
    INDEX `idx_major_class_member_type` (`member_type`),
    CONSTRAINT `fk_major_class_enrollment_major` FOREIGN KEY (`major_id`) REFERENCES `major`(`id`)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_major_class_enrollment_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专业班级成员表';

-- ----------------------------
-- 3. 用户简历表
-- ----------------------------
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
    KEY `idx_is_deleted` (`is_deleted`),
    CONSTRAINT `fk_user_resume_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户简历表';

SET FOREIGN_KEY_CHECKS = 1;
COMMIT;
