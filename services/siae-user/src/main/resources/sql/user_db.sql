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

-- 学籍关联（按你的“简化”方案保留 member_type/status 字段）
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

-- =============================================================================
-- 测试数据填充
-- =============================================================================
INSERT INTO `major` (`id`, `name`, `code`, `abbr`, `college_name`) VALUES
    (201, '软件技术', 'SE', '软件', '信息工程学院'),
    (202, '移动应用开发', 'MAD', '移动', '信息工程学院'),
    (203, '商务英语', 'BE', '商英', '外国语学院');

INSERT INTO `department` (`id`, `name`) VALUES
    (300, '主席团'),
    (301, 'JAVA部'),
    (302, 'Python部'),
    (303, 'C部'),
    (304, '区块链部'),
    (305, 'Web部'),
    (306, '移动应用部');

INSERT INTO `position` (`id`, `name`) VALUES
    (400, '会长'),
    (401, 'JAVA部部长'),
    (402, 'Python部部长'),
    (403, 'C部部长'),
    (404, '区块链部长'),
    (405, 'Web部长'),
    (406, '移动应用部长'),
    (499, '普通成员');

INSERT INTO `award_level` (`id`, `name`) VALUES
    (501, '国家级'),
    (502, '省级'),
    (503, '校级');

INSERT INTO `award_type` (`id`, `name`) VALUES
    (601, '学科竞赛'),
    (602, '创新创业'),
    (603, '文体艺术');

-- 用户（添加学号和头像字段）
INSERT INTO `user` (`id`, `username`, `password`, `student_id`, `avatar_file_id`, `status`) VALUES
    (1, 'president', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2022010101', 'avatar-file-0001', 1),
    (2, 'java_minister', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2022010102', 'avatar-file-0002', 1),
    (3, 'python_minister', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2022010103', 'avatar-file-0003', 1),
    (4, 'c_minister', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2022010104', 'avatar-file-0004', 1),
    (5, 'java_member_01', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2023010101', 'avatar-file-0005', 1),
    (6, 'python_member_01', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2023010102', 'avatar-file-0006', 1),
    (7, 'c_member_01', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2023010103', 'avatar-file-0007', 1),
    (8, 'candidate_01', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2023020101', 'avatar-file-0008', 1),
    (9, 'candidate_02', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2023020202', 'avatar-file-0009', 1),
    (10, 'normal_student', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2023030101', 'avatar-file-0010', 1),
    (11, 'blockchain_minister', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2022010105', 'avatar-file-0011', 1),
    (12, 'web_minister', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2022010106', 'avatar-file-0012', 1),
    (13, 'mobile_minister', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2022010107', 'avatar-file-0013', 1);

-- 用户详情（头像已移至user表）
INSERT INTO `user_profile` (
    `user_id`, `nickname`, `real_name`, `background_file_id`,
    `bio`, `email`, `phone`, `gender`, `birthday`
) VALUES
    (1, '会长大人', '李华', 'bg-file-0001', '协会的领航者', 'lihua@siae.com', '13800138001', 1, '2002-01-15'),
    (2, 'Java大师', '张伟', 'bg-file-0002', 'Everything is an object.', 'zhangwei@siae.com', '13800138002', 1, '2003-03-10'),
    (3, 'Pythonista', '王芳', 'bg-file-0003', '人生苦短，我用Python。', 'wangfang@siae.com', '13800138003', 2, '2003-05-20'),
    (4, 'C语言之父(自封)', '刘强', 'bg-file-0004', '指针，指针，还是指针。', 'liuqiang@siae.com', '13800138004', 1, '2003-07-01'),
    (5, 'Java小兵', '赵敏', 'bg-file-0005', '努力学习Java中...', 'zhaomin@siae.com', '13800138005', 2, '2004-09-01'),
    (6, 'Python新手', '孙琳', 'bg-file-0006', 'print("Hello World")', 'sunlin@siae.com', '13800138006', 2, '2004-10-10'),
    (7, 'C-Learner', '周杰', NULL, NULL, 'zhoujie@siae.com', '13800138007', 1, '2004-11-11'),
    (8, '小萌新A', '吴迪', NULL, '希望加入JAVA部', 'wudi@siae.com', '13800138008', 1, '2005-01-20'),
    (9, '小萌新B', '郑雪', NULL, '对C语言很感兴趣', 'zhengxue@siae.com', '13800138009', 2, '2005-02-28'),
    (10, '路人甲', '冯程', NULL, '我只是一个普通学生', 'fengcheng@siae.com', '13800138010', 1, '2004-06-06'),
    (11, '链圈大佬', '陈链', 'bg-file-0011', 'To the moon!', 'chenlian@siae.com', '13800138011', 1, '2003-04-05'),
    (12, '前端高手', '朱倩', 'bg-file-0012', 'CSS是世界上最好的语言', 'zhuqian@siae.com', '13800138012', 2, '2003-08-15'),
    (13, 'App开发者', '蒋鑫', 'bg-file-0013', 'Android & iOS', 'jiangxin@siae.com', '13800138013', 1, '2003-10-25');

-- 班级关联（简化 member_type/status）
INSERT INTO `major_class_enrollment` (`major_id`, `entry_year`, `class_no`, `user_id`, `member_type`, `status`) VALUES
    (201, 2022, 1, 1, 1, 1),
    (201, 2022, 1, 2, 1, 1),
    (201, 2022, 1, 3, 1, 1),
    (201, 2022, 1, 4, 1, 1),
    (201, 2023, 1, 5, 1, 1),
    (201, 2023, 1, 6, 1, 1),
    (201, 2023, 1, 7, 1, 1),
    (201, 2023, 1, 8, 1, 1),
    (202, 2023, 1, 9, 1, 1),
    (203, 2023, 1, 10, 0, 1),
    (201, 2022, 1, 11, 1, 1),
    (201, 2022, 1, 12, 1, 1),
    (201, 2022, 1, 13, 1, 1);

-- 成员统一表（合并原 member 和 member_candidate）
-- 正式成员：lifecycle_status=1
INSERT INTO `membership` (`user_id`, `headshot_file_id`, `lifecycle_status`, `join_date`) VALUES
    (1, 'headshot-file-0001', 1, '2022-09-01'),
    (2, 'headshot-file-0002', 1, '2022-09-10'),
    (3, 'headshot-file-0003', 1, '2022-09-10'),
    (4, 'headshot-file-0004', 1, '2022-09-10'),
    (5, 'headshot-file-0005', 1, '2023-09-15'),
    (6, 'headshot-file-0006', 1, '2023-09-15'),
    (7, 'headshot-file-0007', 1, '2023-09-15'),
    (11, 'headshot-file-0008', 1, '2022-09-10'),
    (12, 'headshot-file-0009', 1, '2022-09-10'),
    (13, 'headshot-file-0010', 1, '2022-09-10');

-- 候选成员：lifecycle_status=0
INSERT INTO `membership` (`user_id`, `headshot_file_id`, `lifecycle_status`, `join_date`) VALUES
    (8, NULL, 0, NULL),
    (9, NULL, 0, NULL);

-- 部门归属（使用 membership.id）
INSERT INTO `member_department` (`membership_id`, `department_id`, `join_date`, `has_position`) VALUES
    (1, 300, '2022-09-01', 1),
    (2, 301, '2022-09-10', 1),
    (3, 302, '2022-09-10', 1),
    (4, 303, '2022-09-10', 1),
    (5, 301, '2023-09-15', 0),
    (6, 302, '2023-09-15', 0),
    (7, 303, '2023-09-15', 0),
    (8, 304, '2022-09-10', 1),
    (9, 305, '2022-09-10', 1),
    (10, 306, '2022-09-10', 1),
    (11, 301, '2023-10-01', 0),
    (12, 303, '2023-10-02', 0);

-- 职位记录（使用 membership.id）
INSERT INTO `member_position` (`membership_id`, `position_id`, `department_id`, `start_date`, `end_date`) VALUES
    (1, 400, NULL, '2022-09-01', NULL),
    (2, 401, 301, '2022-09-10', NULL),
    (3, 402, 302, '2022-09-10', NULL),
    (4, 403, 303, '2022-09-10', NULL),
    (5, 499, 301, '2023-09-15', NULL),
    (6, 499, 302, '2023-09-15', NULL),
    (7, 499, 303, '2023-09-15', NULL),
    (8, 404, 304, '2022-09-10', NULL),
    (9, 405, 305, '2022-09-10', NULL),
    (10, 406, 306, '2022-09-10', NULL);

-- 获奖记录
-- 获奖记录（team_members包含所有获奖成员，不再需要user_id字段）
INSERT INTO `user_award` (
    `award_title`, `award_level_id`, `award_type_id`,
    `awarded_by`, `awarded_at`, `certificate_file_id`, `team_members`
) VALUES
    ('全国大学生服务外包创新创业大赛', 501, 602, '教育部', '2024-05-20', 'certificate-file-0001', JSON_ARRAY(1, 2, 3, 5)),
    ('蓝桥杯软件类全国总决赛一等奖', 501, 601, '工业和信息化部人才交流中心', '2024-06-01', 'certificate-file-0002', JSON_ARRAY(1, 2, 6, 7));

SET FOREIGN_KEY_CHECKS = 1;
COMMIT;
