-- =============================================================================
-- SIAE (软件协会官网) - 用户数据库 (user_db)
-- 版本: 3.0
-- 描述: 包含优化后的完整表结构和根据要求定制的详细测试数据。
-- =============================================================================

-- ----------------------------
-- 初始化数据库
-- ----------------------------
DROP DATABASE IF EXISTS user_db;
CREATE DATABASE IF NOT EXISTS user_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE user_db;

-- ----------------------------
-- 1. 数据字典表 (Lookup Tables)
-- 必须先创建这些表，因为其他表依赖它们。
-- ----------------------------
CREATE TABLE `college` (
  `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(64) NOT NULL COMMENT '学院名称',
  `code` VARCHAR(32) NOT NULL COMMENT '学院编码',
  UNIQUE KEY `uk_college_name` (`name`),
  UNIQUE KEY `uk_college_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学院字典表';

CREATE TABLE `major` (
  `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `college_id` BIGINT UNSIGNED NOT NULL COMMENT '所属学院ID',
  `name` VARCHAR(64) NOT NULL COMMENT '专业名称',
  `code` VARCHAR(32) NOT NULL COMMENT '专业编码',
  `abbr` VARCHAR(16) COMMENT '专业简称',
  UNIQUE KEY `uk_major_name` (`name`),
  UNIQUE KEY `uk_major_code` (`code`),
  CONSTRAINT `fk_major_college` FOREIGN KEY (`college_id`) REFERENCES `college` (`id`)
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
  `status` TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
  `avatar` VARCHAR(512) COMMENT '头像URL',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否逻辑删除：0否，1是',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_username` (`username`),
  INDEX `idx_status` (`status`),
  INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户主表';

CREATE TABLE `user_profile` (
  `user_id` BIGINT UNSIGNED PRIMARY KEY COMMENT '外键，关联user表',
  `nickname` VARCHAR(64) COMMENT '昵称',
  `real_name` VARCHAR(64) COMMENT '真实姓名',
  `bio` TEXT COMMENT '个人简介',
  `bg_url` VARCHAR(512) COMMENT '主页背景图片URL',
  `email` VARCHAR(128) COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '手机',
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
  CONSTRAINT `fk_profile_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户详情表';

CREATE TABLE `user_third_party_auth` (
  `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '关联到我们系统内的用户ID',
  `provider` VARCHAR(50) NOT NULL COMMENT '第三方平台名称 (如: ''wechat'', ''qq'')',
  `provider_user_id` VARCHAR(255) NOT NULL COMMENT '用户在第三方平台的唯一ID (如 openid)',
  `nickname_on_provider` VARCHAR(255) COMMENT '用户在第三方平台的昵称 (冗余)',
  `avatar_on_provider` VARCHAR(512) COMMENT '用户在第三方平台的头像URL (冗余)',
  `access_token` VARCHAR(512) COMMENT '第三方平台的访问令牌 (加密存储)',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_provider_user` (`provider`, `provider_user_id`),
  CONSTRAINT `fk_third_party_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第三方认证表';

CREATE TABLE `class` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '班级ID',
  `college_id` BIGINT UNSIGNED NOT NULL COMMENT '关联学院ID',
  `major_id` BIGINT UNSIGNED NOT NULL COMMENT '关联专业ID',
  `year` INT NOT NULL COMMENT '入学年份',
  `class_no` INT NOT NULL COMMENT '班号',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0否，1是',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_year_major_class` (`year`, `major_id`, `class_no`),
  INDEX `idx_is_deleted` (`is_deleted`),
  CONSTRAINT `fk_class_college` FOREIGN KEY (`college_id`) REFERENCES `college` (`id`),
  CONSTRAINT `fk_class_major` FOREIGN KEY (`major_id`) REFERENCES `major` (`id`)
) COMMENT='班级表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `member` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '成员ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '关联用户ID',
  `student_id` VARCHAR(32) NOT NULL COMMENT '学号',
  `department_id` BIGINT UNSIGNED NOT NULL COMMENT '关联部门ID',
  `position_id` BIGINT UNSIGNED NOT NULL COMMENT '关联职位ID',
  `join_date` DATE NOT NULL COMMENT '加入日期',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1在校，2离校，3毕业',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0否，1是',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_user_id` (`user_id`),
  UNIQUE KEY `uk_student_id` (`student_id`),
  INDEX `idx_is_deleted` (`is_deleted`),
  CONSTRAINT `fk_member_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_member_department` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`),
  CONSTRAINT `fk_member_position` FOREIGN KEY (`position_id`) REFERENCES `position` (`id`)
) COMMENT='成员表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `member_candidate` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '候选成员ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '关联用户ID',
  `student_id` VARCHAR(32) NOT NULL COMMENT '学号',
  `department_id` BIGINT UNSIGNED NOT NULL COMMENT '关联意向部门ID',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0待审核，1通过，2拒绝',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0否，1是',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_user_id` (`user_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_is_deleted` (`is_deleted`),
  CONSTRAINT `fk_candidate_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_candidate_department` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`)
) COMMENT='候选成员表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `user_award` (
  `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键，自增',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `award_title` VARCHAR(255) NOT NULL COMMENT '奖项名称',
  `award_level_id` BIGINT UNSIGNED NOT NULL COMMENT '关联奖项等级ID',
  `award_type_id` BIGINT UNSIGNED NOT NULL COMMENT '关联奖项类型ID',
  `awarded_by` VARCHAR(255) NOT NULL COMMENT '颁发单位',
  `awarded_at` DATE NOT NULL COMMENT '获奖时间',
  `description` TEXT COMMENT '获奖描述（选填）',
  `certificate_url` VARCHAR(512) COMMENT '奖状或证明材料的URL',
  `team_members` TEXT COMMENT '团队成员信息',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0否，1是',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_is_deleted` (`is_deleted`),
  CONSTRAINT `fk_award_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_award_level` FOREIGN KEY (`award_level_id`) REFERENCES `award_level`(`id`),
  CONSTRAINT `fk_award_type` FOREIGN KEY (`award_type_id`) REFERENCES `award_type`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户获奖记录表';

CREATE TABLE `class_user` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `class_id` BIGINT NOT NULL COMMENT '班级ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1在读，2转班，3毕业',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0否，1是',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_class_user` (`class_id`, `user_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_is_deleted` (`is_deleted`),
  CONSTRAINT `fk_class_user_class` FOREIGN KEY (`class_id`) REFERENCES `class`(`id`),
  CONSTRAINT `fk_class_user_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) COMMENT='班级与用户关联表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =============================================================================
-- 测试数据填充
-- =============================================================================

-- 0. 填充字典表
INSERT INTO `college` (`id`, `name`, `code`) VALUES
(101, '信息工程学院', 'CIE'),
(102, '外国语学院', 'FL');

INSERT INTO `major` (`id`, `college_id`, `name`, `code`, `abbr`) VALUES
(201, 101, '软件技术', 'SE', '软件'),
(202, 101, '移动应用开发', 'MAD', '移动'),
(203, 102, '商务英语', 'BE', '商英');
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

-- 1. 插入用户数据
-- 密码均为 '123456' 的BCrypt哈希值: $2a$10$N.zmdr9k7uOCQb0bzysuAOyoyNpwSr0YHiXKuNTtDB6aANfGDx9he
INSERT INTO `user` (`id`, `username`, `password`, `status`) VALUES
(1, 'president', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', 1),
(2, 'java_minister', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', 1),
(3, 'python_minister', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', 1),
(4, 'c_minister', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', 1),
(5, 'java_member_01', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', 1),
(6, 'python_member_01', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', 1),
(7, 'c_member_01', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', 1),
(8, 'candidate_01', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', 1),
(9, 'candidate_02', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', 1),
(10, 'normal_student', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', 1),
(11, 'blockchain_minister', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', 1),
(12, 'web_minister', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', 1),
(13, 'mobile_minister', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', 1);

-- 2. 插入用户详情数据
INSERT INTO `user_profile` (`user_id`, `nickname`, `real_name`, `bg_url`, `bio`, `email`, `phone`, `gender`, `birthday`) VALUES
(1, '会长大人', '李华', 'https://placehold.co/100x100/F56C6C/FFFFFF?text=会长', '协会的领航者', 'lihua@siae.com', '13800138001', 1, '2002-01-15'),
(2, 'Java大师', '张伟', 'https://placehold.co/100x100/E6A23C/FFFFFF?text=Java', 'Everything is an object.', 'zhangwei@siae.com', '13800138002', 1, '2003-03-10'),
(3, 'Pythonista', '王芳', 'https://placehold.co/100x100/409EFF/FFFFFF?text=Py', '人生苦短，我用Python。', 'wangfang@siae.com', '13800138003', 2, '2003-05-20'),
(4, 'C语言之父(自封)', '刘强', 'https://placehold.co/100x100/67C23A/FFFFFF?text=C', '指针，指针，还是指针。', 'liuqiang@siae.com', '13800138004', 1, '2003-07-01'),
(5, 'Java小兵', '赵敏', 'https://placehold.co/100x100/909399/FFFFFF?text=JM', '努力学习Java中...', 'zhaomin@siae.com', '13800138005', 2, '2004-09-01'),
(6, 'Python新手', '孙琳', 'https://placehold.co/100x100/909399/FFFFFF?text=PyM', 'print("Hello World")', 'sunlin@siae.com', '13800138006', 2, '2004-10-10'),
(7, 'C-Learner', '周杰', 'https://placehold.co/100x100/909399/FFFFFF?text=CM', NULL, 'zhoujie@siae.com', '13800138007', 1, '2004-11-11'),
(8, '小萌新A', '吴迪', 'https://placehold.co/100x100/E9A8A8/FFFFFF?text=萌新', '希望加入JAVA部', 'wudi@siae.com', '13800138008', 1, '2005-01-20'),
(9, '小萌新B', '郑雪', 'https://placehold.co/100x100/A8DDE9/FFFFFF?text=萌新', '对C语言很感兴趣', 'zhengxue@siae.com', '13800138009', 2, '2005-02-28'),
(10, '路人甲', '冯程', 'https://placehold.co/100x100/CCCCCC/FFFFFF?text=路人', '我只是一个普通学生', 'fengcheng@siae.com', '13800138010', 1, '2004-06-06'),
(11, '链圈大佬', '陈链', 'https://placehold.co/100x100/303133/FFFFFF?text=链', 'To the moon!', 'chenlian@siae.com', '13800138011', 1, '2003-04-05'),
(12, '前端高手', '朱倩', 'https://placehold.co/100x100/303133/FFFFFF?text=Web', 'CSS是世界上最好的语言', 'zhuqian@siae.com', '13800138012', 2, '2003-08-15'),
(13, 'App开发者', '蒋鑫', 'https://placehold.co/100x100/303133/FFFFFF?text=App', 'Android & iOS', 'jiangxin@siae.com', '13800138013', 1, '2003-10-25');

-- 3. 插入班级数据
INSERT INTO `class` (`id`, `college_id`, `major_id`, `year`, `class_no`) VALUES
(1, 101, 201, 2022, 1), -- 信息工程学院-软件技术-2022级-1班
(2, 101, 201, 2023, 1), -- 信息工程学院-软件技术-2023级-1班
(3, 101, 202, 2023, 1), -- 信息工程学院-移动应用开发-2023级-1班
(4, 102, 203, 2023, 1); -- 外国语学院-商务英语-2023级-1班

-- 4. 插入班级与用户关联数据
INSERT INTO `class_user` (`class_id`, `user_id`, `status`) VALUES
(1, 1, 1),  -- 会长 -> 2022级软件技术1班
(1, 2, 1),  -- Java部长 -> 2022级软件技术1班
(1, 3, 1),  -- Python部长 -> 2022级软件技术1班
(1, 4, 1),  -- C部长 -> 2022级软件技术1班
(2, 5, 1),  -- Java成员 -> 2023级软件技术1班
(2, 6, 1),  -- Python成员 -> 2023级软件技术1班
(2, 7, 1),  -- C成员 -> 2023级软件技术1班
(2, 8, 1),  -- 候选成员A -> 2023级软件技术1班
(3, 9, 1),  -- 候选成员B -> 2023级移动应用开发1班
(4, 10, 1), -- 普通学生 -> 2023级商务英语1班
(1, 11, 1), -- 区块链部长 -> 2022级软件技术1班
(1, 12, 1), -- Web部长 -> 2022级软件技术1班
(1, 13, 1); -- 移动应用部长 -> 2022级软件技术1班

-- 5. 插入成员数据
INSERT INTO `member` (`user_id`, `student_id`, `department_id`, `position_id`, `join_date`, `status`) VALUES
(1, '2022010101', 300, 400, '2022-09-01', 1), -- 李华 - 会长 - 主席团
(2, '2022010102', 301, 401, '2022-09-10', 1), -- 张伟 - JAVA部部长 - JAVA部
(3, '2022010103', 302, 402, '2022-09-10', 1), -- 王芳 - Python部部长 - Python部
(4, '2022010104', 303, 403, '2022-09-10', 1), -- 刘强 - C部部长 - C部
(5, '2023010101', 301, 499, '2023-09-15', 1), -- 赵敏 - 普通成员 - JAVA部
(6, '2023010102', 302, 499, '2023-09-15', 1), -- 孙琳 - 普通成员 - Python部
(7, '2023010103', 303, 499, '2023-09-15', 1), -- 周杰 - 普通成员 - C部
(11, '2022010105', 304, 404, '2022-09-10', 1), -- 陈链 - 区块链部长 - 区块链部
(12, '2022010106', 305, 405, '2022-09-10', 1), -- 朱倩 - Web部长 - Web部
(13, '2022010107', 306, 406, '2022-09-10', 1); -- 蒋鑫 - 移动应用部长 - 移动应用部

-- 6. 插入候选成员数据
INSERT INTO `member_candidate` (`user_id`, `student_id`, `department_id`, `status`) VALUES
(8, '2023020101', 301, 0), -- 吴迪 - 申请加入JAVA部 - 待审核
(9, '2023020202', 303, 0); -- 郑雪 - 申请加入C部 - 待审核

-- 7. 插入用户获奖记录数据
INSERT INTO `user_award` (`user_id`, `award_title`, `award_level_id`, `award_type_id`, `awarded_by`, `awarded_at`) VALUES
(1, '全国大学生服务外包创新创业大赛', 501, 602, '教育部', '2024-05-20'), -- 李华 - 国家级创新创业奖项
(2, '蓝桥杯软件类全国总决赛一等奖', 501, 601, '工业和信息化部人才交流中心', '2024-06-01'); -- 张伟 - 国家级学科竞赛奖项

-- 8. 插入第三方认证数据
INSERT INTO `user_third_party_auth` (`user_id`, `provider`, `provider_user_id`) VALUES
(1, 'wechat', 'o6_bmjrPTlm6_2sgVt7hMZOPfL2M'), -- 李华的微信OpenID
(2, 'github', '1234567'); -- 张伟的GitHub ID

COMMIT;
