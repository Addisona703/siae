-- =============================================================================
-- SIAE 微服务项目 - 综合测试数据
-- 版本: 1.0
-- 描述: 包含所有微服务的测试数据，可一键初始化整个系统
-- 创建日期: 2025-12-30
-- 包含服务: user_db, auth_db, content_db, attendance_db, notification_db, media_db, ai_db
-- 注意: 执行前请确保所有数据库表结构已创建
-- =============================================================================

-- =============================================================================
-- 第一部分：用户服务 (user_db)
-- =============================================================================
USE `user_db`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 清空所有表数据
TRUNCATE TABLE `user_award`;
TRUNCATE TABLE `member_position`;
TRUNCATE TABLE `member_department`;
TRUNCATE TABLE `membership`;
TRUNCATE TABLE `major_class_enrollment`;
TRUNCATE TABLE `user_profile`;
TRUNCATE TABLE `user_resume`;
TRUNCATE TABLE `user`;
TRUNCATE TABLE `award_type`;
TRUNCATE TABLE `award_level`;
TRUNCATE TABLE `position`;
TRUNCATE TABLE `department`;
TRUNCATE TABLE `major`;

-- 专业数据（协会只招软件技术和移动应用开发专业）
INSERT INTO `major` (`id`, `name`, `code`, `abbr`, `college_name`) VALUES
    (1, '软件技术', 'SE', '软件', '信息工程学院'),
    (2, '移动应用开发', 'MAD', '移动', '信息工程学院');

-- 部门数据
INSERT INTO `department` (`id`, `name`) VALUES
    (1, 'Java部'),
    (2, 'Python部'),
    (3, 'C部'),
    (4, '区块链部'),
    (5, 'Web部'),
    (6, '移动应用部');

-- 职位数据
INSERT INTO `position` (`id`, `name`) VALUES
    (1, '会长'),
    (2, 'Java部部长'),
    (3, 'Python部部长'),
    (4, 'C部部长'),
    (5, '区块链部部长'),
    (6, 'Web部部长'),
    (7, '移动应用部部长'),
    (8, '普通成员');

-- 奖项等级数据
INSERT INTO `award_level` (`id`, `name`, `order_id`) VALUES
    (1, '国家级', 1),
    (2, '省级', 2),
    (3, '校级', 3);

-- 奖项类型数据
INSERT INTO `award_type` (`id`, `name`, `order_id`) VALUES
    (1, '学科竞赛', 1),
    (2, '创新创业', 2),
    (3, '文体艺术', 3);

-- 用户数据 (密码统一为: 123456，BCrypt加密)
-- id=1 为超级管理员（系统管理员，非协会成员）
-- id=2 为会长
-- 头像随机分配：jo1-jo5, Shinpei Ajiro, Ushio Kofune
INSERT INTO `user` (`id`, `username`, `password`, `student_id`, `avatar_file_id`, `status`) VALUES
    (1, 'root', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', NULL, 'fbd9f8543888fb9370fc52c7397dcf1c', 1),
    (2, 'president', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2022010101', 'f5ec3df8fedfa85d0a102b7cb5874da0', 1),
    (3, 'java_minister', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2022010102', '9237e3109ce88288e2232fdfa04bce55', 1),
    (4, 'python_minister', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2022010103', '7a376e75de1a08fc900d887f2f7ee08e', 1),
    (5, 'c_minister', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2022010104', 'a58cf68724d82a099037e2a17ff9eaa1', 1),
    (6, 'blockchain_minister', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2022010105', 'e7de8240512c5af52ef323a6584c10e3', 1),
    (7, 'web_minister', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2022010106', '0b26cd248846ea4d308f4eb6ee6f2c97', 1),
    (8, 'mobile_minister', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2022010107', 'fbd9f8543888fb9370fc52c7397dcf1c', 1),
    (9, 'java_member_01', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2023010101', 'f5ec3df8fedfa85d0a102b7cb5874da0', 1),
    (10, 'java_member_02', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2023010102', '9237e3109ce88288e2232fdfa04bce55', 1),
    (11, 'python_member_01', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2023010103', '7a376e75de1a08fc900d887f2f7ee08e', 1),
    (12, 'c_member_01', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2023010104', 'a58cf68724d82a099037e2a17ff9eaa1', 1),
    (13, 'web_member_01', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2023010105', 'e7de8240512c5af52ef323a6584c10e3', 1),
    (14, 'candidate_01', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2024010101', '0b26cd248846ea4d308f4eb6ee6f2c97', 1),
    (15, 'candidate_02', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2024010102', 'fbd9f8543888fb9370fc52c7397dcf1c', 1);

-- 用户详情数据
-- 背景图随机分配：forest, gad, ice, mogudi, ocean
INSERT INTO `user_profile` (
    `user_id`, `nickname`, `real_name`, `background_file_id`, `id_card`,
    `bio`, `email`, `phone`, `gender`, `birthday`
) VALUES
    (1, '系统管理员', '管理员', '8613debb3214cbd8f6ff97104a45803a', '110101199001011234', '系统超级管理员', 'admin@siae.com', '13800000000', 1, '1990-01-01'),
    (2, '会长大人', '李华', 'c7dc85dd9e26283cc9fbf7a2478baa99', '110101200301155678', '协会的领航者', 'lihua@siae.com', '13800138001', 1, '2003-01-15'),
    (3, 'Java大师', '张伟', 'eb95710d2841d6d253bea36e7f45d453', '110101200303109012', 'Everything is an object.', 'zhangwei@siae.com', '13800138002', 1, '2003-03-10'),
    (4, 'Pythonista', '王芳', '04fbf782e74a17b03bbc93b6de89331f', '110101200305203456', '人生苦短，我用Python。', 'wangfang@siae.com', '13800138003', 2, '2003-05-20'),
    (5, 'C语言之父', '刘强', '9dea73250f7f9233be7b9d1cbdb113c7', '110101200307017890', '指针，指针，还是指针。', 'liuqiang@siae.com', '13800138004', 1, '2003-07-01'),
    (6, '链圈大佬', '陈链', '8613debb3214cbd8f6ff97104a45803a', '110101200304051234', 'To the moon!', 'chenlian@siae.com', '13800138005', 1, '2003-04-05'),
    (7, '前端高手', '朱倩', 'c7dc85dd9e26283cc9fbf7a2478baa99', '110101200308155678', 'CSS是世界上最好的语言', 'zhuqian@siae.com', '13800138006', 2, '2003-08-15'),
    (8, 'App开发者', '蒋鑫', 'eb95710d2841d6d253bea36e7f45d453', '110101200310259012', 'Android & iOS', 'jiangxin@siae.com', '13800138007', 1, '2003-10-25'),
    (9, 'Java小兵', '赵敏', '04fbf782e74a17b03bbc93b6de89331f', '110101200409013456', '努力学习Java中...', 'zhaomin@siae.com', '13800138008', 2, '2004-09-01'),
    (10, 'Spring达人', '林晓', '9dea73250f7f9233be7b9d1cbdb113c7', '110101200403127890', 'Spring Boot专家', 'linxiao@siae.com', '13800138009', 2, '2004-03-12'),
    (11, 'Python新手', '孙琳', '8613debb3214cbd8f6ff97104a45803a', '110101200410101234', 'print("Hello World")', 'sunlin@siae.com', '13800138010', 2, '2004-10-10'),
    (12, 'C-Learner', '周杰', 'c7dc85dd9e26283cc9fbf7a2478baa99', '110101200411115678', 'C语言爱好者', 'zhoujie@siae.com', '13800138011', 1, '2004-11-11'),
    (13, 'Vue专家', '袁梦', 'eb95710d2841d6d253bea36e7f45d453', '110101200411059012', '前端架构师', 'yuanmeng@siae.com', '13800138012', 2, '2004-11-05'),
    (14, '小萌新A', '吴迪', '04fbf782e74a17b03bbc93b6de89331f', '110101200501203456', '希望加入Java部', 'wudi@siae.com', '13800138013', 1, '2005-01-20'),
    (15, '小萌新B', '郑雪', '9dea73250f7f9233be7b9d1cbdb113c7', '110101200502287890', '对Python很感兴趣', 'zhengxue@siae.com', '13800138014', 2, '2005-02-28');

-- 班级关联数据（只有软件技术和移动应用开发专业）
-- major_id: 1=软件技术, 2=移动应用开发
-- member_type: 0=非协会成员, 1=协会成员
-- status: 1=在读, 2=离校
INSERT INTO `major_class_enrollment` (`major_id`, `entry_year`, `class_no`, `user_id`, `member_type`, `status`) VALUES
    (1, 2022, 1, 2, 1, 1),   -- 会长-软件技术
    (1, 2022, 1, 3, 1, 1),   -- Java部长
    (1, 2022, 2, 4, 1, 1),   -- Python部长
    (1, 2022, 1, 5, 1, 1),   -- C部长
    (2, 2022, 1, 6, 1, 1),   -- 区块链部长-移动应用开发
    (1, 2022, 2, 7, 1, 1),   -- Web部长
    (2, 2022, 1, 8, 1, 1),   -- 移动应用部长
    (1, 2023, 1, 9, 1, 1),   -- Java成员
    (1, 2023, 2, 10, 1, 1),  -- Java成员
    (1, 2023, 1, 11, 1, 1),  -- Python成员
    (2, 2023, 1, 12, 1, 1),  -- C成员
    (1, 2023, 2, 13, 1, 1),  -- Web成员
    (1, 2024, 1, 14, 1, 1),  -- 候选成员
    (2, 2024, 1, 15, 1, 1);  -- 候选成员

-- 成员统一表数据
-- lifecycle_status: 0=待审核, 1=候选成员, 2=正式成员, 3=已拒绝, 4=已开除
-- 注意：超级管理员(id=1)不是协会成员，不在此表中
-- 大头照按性别分配：Steve(男), Robin(女)
INSERT INTO `membership` (`id`, `user_id`, `headshot_file_id`, `lifecycle_status`, `join_date`) VALUES
    (1, 2, '0b3537cfd9214887b032b75d1dd562a5', 2, '2022-09-01'),   -- 会长-正式成员-男-Steve
    (2, 3, '0b3537cfd9214887b032b75d1dd562a5', 2, '2022-09-10'),   -- Java部长-正式成员-男-Steve
    (3, 4, 'a5bfb3eb3e17ba5fbb0458d22e0435ca', 2, '2022-09-10'),   -- Python部长-正式成员-女-Robin
    (4, 5, '0b3537cfd9214887b032b75d1dd562a5', 2, '2022-09-10'),   -- C部长-正式成员-男-Steve
    (5, 6, '0b3537cfd9214887b032b75d1dd562a5', 2, '2022-09-10'),   -- 区块链部长-正式成员-男-Steve
    (6, 7, 'a5bfb3eb3e17ba5fbb0458d22e0435ca', 2, '2022-09-10'),   -- Web部长-正式成员-女-Robin
    (7, 8, '0b3537cfd9214887b032b75d1dd562a5', 2, '2022-09-10'),   -- 移动应用部长-正式成员-男-Steve
    (8, 9, 'a5bfb3eb3e17ba5fbb0458d22e0435ca', 2, '2023-09-15'),   -- Java成员-正式成员-女-Robin
    (9, 10, 'a5bfb3eb3e17ba5fbb0458d22e0435ca', 2, '2023-09-15'),  -- Java成员-正式成员-女-Robin
    (10, 11, 'a5bfb3eb3e17ba5fbb0458d22e0435ca', 2, '2023-09-15'), -- Python成员-正式成员-女-Robin
    (11, 12, '0b3537cfd9214887b032b75d1dd562a5', 2, '2023-09-15'), -- C成员-正式成员-男-Steve
    (12, 13, 'a5bfb3eb3e17ba5fbb0458d22e0435ca', 2, '2023-09-15'), -- Web成员-正式成员-女-Robin
    (13, 14, NULL, 1, '2024-11-01'),            -- 候选成员（无大头照）-男
    (14, 15, NULL, 1, '2024-11-01');            -- 候选成员（无大头照）-女

-- 部门归属数据
-- department_id: 1=Java部, 2=Python部, 3=C部, 4=区块链部, 5=Web部, 6=移动应用部
INSERT INTO `member_department` (`membership_id`, `department_id`, `join_date`, `has_position`) VALUES
    (2, 1, '2022-09-10', 1),   -- Java部长
    (3, 2, '2022-09-10', 1),   -- Python部长
    (4, 3, '2022-09-10', 1),   -- C部长
    (5, 4, '2022-09-10', 1),   -- 区块链部长
    (6, 5, '2022-09-10', 1),   -- Web部长
    (7, 6, '2022-09-10', 1),   -- 移动应用部长
    (8, 1, '2023-09-15', 0),   -- Java成员
    (9, 1, '2023-09-15', 0),   -- Java成员
    (10, 2, '2023-09-15', 0),  -- Python成员
    (11, 3, '2023-09-15', 0),  -- C成员
    (12, 5, '2023-09-15', 0);  -- Web成员

-- 职位记录数据
-- position_id: 1=会长, 2=Java部部长, 3=Python部部长, 4=C部部长, 5=区块链部部长, 6=Web部部长, 7=移动应用部部长, 8=普通成员
INSERT INTO `member_position` (`membership_id`, `position_id`, `department_id`, `start_date`, `end_date`) VALUES
    (1, 1, NULL, '2022-09-01', NULL),    -- 会长（全协会职位，无部门）
    (2, 2, 1, '2022-09-10', NULL),       -- Java部部长
    (3, 3, 2, '2022-09-10', NULL),       -- Python部部长
    (4, 4, 3, '2022-09-10', NULL),       -- C部部长
    (5, 5, 4, '2022-09-10', NULL),       -- 区块链部部长
    (6, 6, 5, '2022-09-10', NULL),       -- Web部部长
    (7, 7, 6, '2022-09-10', NULL),       -- 移动应用部部长
    (8, 8, 1, '2023-09-15', NULL),       -- Java部普通成员
    (9, 8, 1, '2023-09-15', NULL),       -- Java部普通成员
    (10, 8, 2, '2023-09-15', NULL),      -- Python部普通成员
    (11, 8, 3, '2023-09-15', NULL),      -- C部普通成员
    (12, 8, 5, '2023-09-15', NULL);      -- Web部普通成员

-- 获奖记录数据
INSERT INTO `user_award` (
    `award_title`, `award_level_id`, `award_type_id`,
    `awarded_by`, `awarded_at`, `certificate_file_id`, `team_members`
) VALUES
    -- 蓝桥杯（单人赛）
    ('蓝桥杯软件类全国总决赛一等奖', 1, 1, '工业和信息化部人才交流中心', '2024-12-01', '56ccb8887a72f083ac355c3708840fbe', JSON_ARRAY(2)),
    ('蓝桥杯软件类省赛一等奖', 2, 1, '工业和信息化部人才交流中心', '2024-04-15', '56ccb8887a72f083ac355c3708840fbe', JSON_ARRAY(3)),
    ('蓝桥杯软件类省赛二等奖', 2, 1, '工业和信息化部人才交流中心', '2024-04-15', '56ccb8887a72f083ac355c3708840fbe', JSON_ARRAY(9)),
    
    -- 程序设计竞赛（单人赛）
    ('省大学生程序设计竞赛一等奖', 2, 1, '省教育厅', '2024-11-20', 'e5b94fb341133d3b39146818e9bf52a5', JSON_ARRAY(2)),
    ('省大学生程序设计竞赛二等奖', 2, 1, '省教育厅', '2024-11-20', 'e5b94fb341133d3b39146818e9bf52a5', JSON_ARRAY(3)),
    ('省大学生程序设计竞赛三等奖', 2, 1, '省教育厅', '2024-11-20', 'e5b94fb341133d3b39146818e9bf52a5', JSON_ARRAY(5)),
    ('校程序设计大赛一等奖', 3, 1, '校团委', '2024-11-15', 'd53e6790b14fec2d125c741d68ff2841', JSON_ARRAY(3)),
    ('校程序设计大赛二等奖', 3, 1, '校团委', '2024-11-15', 'd53e6790b14fec2d125c741d68ff2841', JSON_ARRAY(9)),
    ('校程序设计大赛三等奖', 3, 1, '校团委', '2024-11-15', 'd53e6790b14fec2d125c741d68ff2841', JSON_ARRAY(11)),
    
    -- 创新创业（团队赛）
    ('省"互联网+"大学生创新创业大赛金奖', 2, 2, '省教育厅', '2024-12-15', 'e5b94fb341133d3b39146818e9bf52a5', JSON_ARRAY(2, 4, 11)),
    ('省"互联网+"大学生创新创业大赛银奖', 2, 2, '省教育厅', '2024-12-15', 'e5b94fb341133d3b39146818e9bf52a5', JSON_ARRAY(3, 7, 13));

-- 用户简历数据
INSERT INTO `user_resume` (
    `user_id`, `avatar`, `name`, `gender`, `age`, `work_status`, `phone`, `wechat`,
    `job_status`, `graduation_year`, `expected_jobs`, `advantages`,
    `work_experience`, `projects`, `education`, `awards`
) VALUES
    (3, 'https://example.com/avatar/zhangwei.jpg', '张伟', '男', 22, '学生', '13800138002', 'zhangwei_wx',
     '在校·考虑机会', '2025', 
     '[{"salary":"10-15K","industry":"互联网","location":"北京","position":"Java开发工程师","positionDetail":"Java后端开发"}]',
     '熟练掌握Java、Spring Boot、MySQL等技术栈，有丰富的项目经验。精通Spring全家桶，了解微服务架构。具备良好的代码规范和团队协作能力。',
     '[{"name":"某科技公司","role":"Java实习生","time":"2024-07 ~ 2024-09","description":"参与后端开发，负责用户模块和权限系统的开发与维护"}]',
     '[{"name":"SIAE官网","role":"后端开发","time":"2024-03 ~ 2024-06","description":"负责用户模块、内容管理模块的开发，使用Spring Boot + MyBatis Plus技术栈"}]',
     '[{"time":"2022-09 ~ 2025-06","major":"软件技术","degree":"专科","school":"XX职业技术学院","courses":"Java程序设计、数据库原理、Web开发"}]',
     '[{"name":"蓝桥杯省赛一等奖","time":"2024-04"}]'),
    
    (4, 'https://example.com/avatar/wangfang.jpg', '王芳', '女', 22, '学生', '13800138003', 'wangfang_wx',
     '在校·考虑机会', '2025',
     '[{"salary":"8-12K","industry":"数据分析","location":"上海","position":"Python开发工程师","positionDetail":"Python数据分析"}]',
     '熟练使用Python进行数据分析和处理，掌握Pandas、NumPy、Matplotlib等数据分析工具。了解机器学习基础算法，有实际项目经验。',
     '[{"name":"数据分析实习","role":"数据分析实习生","time":"2024-06 ~ 2024-08","description":"负责数据清洗、数据可视化和报表生成工作"}]',
     '[{"name":"学生成绩分析系统","role":"项目负责人","time":"2024-01 ~ 2024-05","description":"使用Python进行学生成绩数据分析，生成可视化报表"}]',
     '[{"time":"2022-09 ~ 2025-06","major":"软件技术","degree":"专科","school":"XX职业技术学院","courses":"Python程序设计、数据分析、机器学习基础"}]',
     '[{"name":"省大学生数学建模竞赛二等奖","time":"2024-05"}]'),
    
    (9, 'https://example.com/avatar/zhaomin.jpg', '赵敏', '女', 21, '学生', '13800138008', 'zhaomin_wx',
     '在校·积极求职', '2026',
     '[{"salary":"6-10K","industry":"互联网","location":"北京","position":"Java开发实习生","positionDetail":"Java"}]',
     '正在学习Java开发，掌握Java基础、Spring框架、MySQL数据库。有较强的学习能力和团队协作精神，希望找到一份Java开发实习工作。',
     '[]',
     '[{"name":"在线图书管理系统","role":"开发者","time":"2024-09 ~ 2024-12","description":"使用Spring Boot开发的图书管理系统，实现了图书的增删改查、借阅管理等功能"}]',
     '[{"time":"2023-09 ~ 2026-06","major":"软件技术","degree":"专科","school":"XX职业技术学院","courses":"Java程序设计、Web开发、数据库应用"}]',
     '[{"name":"校程序设计大赛三等奖","time":"2024-11"}]'),
    
    (11, 'https://example.com/avatar/sunlin.jpg', '孙琳', '女', 21, '学生', '13800138010', 'sunlin_wx',
     '在校·考虑机会', '2026',
     '[{"salary":"7-10K","industry":"企业服务","location":"上海","position":"Python开发","positionDetail":"Python"}]',
     '学习Python开发中，掌握Python基础语法、Flask框架、数据库操作。对Web开发和数据处理有浓厚兴趣，希望在实践中提升技能。',
     '[]',
     '[{"name":"个人博客系统","role":"开发者","time":"2024-10 ~ 2024-12","description":"使用Flask开发的个人博客系统，实现了文章发布、评论、标签等功能"}]',
     '[{"time":"2023-09 ~ 2026-06","major":"软件技术","degree":"专科","school":"XX职业技术学院","courses":"Python程序设计、Web开发、数据库原理"}]',
     '[]'),
    
    (12, 'https://example.com/avatar/zhoujie.jpg', '周杰', '男', 21, '学生', '13800138011', 'zhoujie_wx',
     '在校·考虑机会', '2026',
     '[{"salary":"8-10K","industry":"游戏","location":"深圳","position":"C++开发","positionDetail":"C/C++"}]',
     '熟悉C/C++编程，了解数据结构和算法。对游戏开发和底层编程有兴趣，正在学习游戏引擎开发相关知识。',
     '[]',
     '[{"name":"简易游戏引擎","role":"开发者","time":"2024-08 ~ 2024-12","description":"使用C++开发的2D游戏引擎，实现了基本的渲染、碰撞检测等功能"}]',
     '[{"time":"2023-09 ~ 2026-06","major":"移动应用开发","degree":"专科","school":"XX职业技术学院","courses":"C语言程序设计、数据结构、算法设计"}]',
     '[{"name":"ACM校赛铜奖","time":"2024-10"}]');

SET FOREIGN_KEY_CHECKS = 1;


-- =============================================================================
-- 第二部分：认证服务 (auth_db)
-- =============================================================================
USE `auth_db`;

SET FOREIGN_KEY_CHECKS = 0;

-- 清空关联表数据（保留角色和权限基础数据）
TRUNCATE TABLE `login_log`;
TRUNCATE TABLE `user_auth`;
TRUNCATE TABLE `oauth_account`;
TRUNCATE TABLE `user_permission`;

-- 清空用户角色关联表（完全重建）
TRUNCATE TABLE `user_role`;

SET FOREIGN_KEY_CHECKS = 1;

-- 用户角色关联（与user_db中的用户对应）
-- root(1)=超级管理员, president(2)=管理员, 部长(3-8)=协会成员, 普通成员(9-13)=协会成员, 候选(14-15)=普通用户
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES
    (1, (SELECT id FROM `role` WHERE code = 'ROLE_ROOT')),
    (2, (SELECT id FROM `role` WHERE code = 'ROLE_ADMIN')),
    (3, (SELECT id FROM `role` WHERE code = 'ROLE_ADMIN')),
    (4, (SELECT id FROM `role` WHERE code = 'ROLE_ADMIN')),
    (5, (SELECT id FROM `role` WHERE code = 'ROLE_ADMIN')),
    (6, (SELECT id FROM `role` WHERE code = 'ROLE_ADMIN')),
    (7, (SELECT id FROM `role` WHERE code = 'ROLE_ADMIN')),
    (8, (SELECT id FROM `role` WHERE code = 'ROLE_ADMIN')),
    (9, (SELECT id FROM `role` WHERE code = 'ROLE_MEMBER')),
    (10, (SELECT id FROM `role` WHERE code = 'ROLE_MEMBER')),
    (11, (SELECT id FROM `role` WHERE code = 'ROLE_MEMBER')),
    (12, (SELECT id FROM `role` WHERE code = 'ROLE_MEMBER')),
    (13, (SELECT id FROM `role` WHERE code = 'ROLE_MEMBER')),
    (14, (SELECT id FROM `role` WHERE code = 'ROLE_USER')),
    (15, (SELECT id FROM `role` WHERE code = 'ROLE_USER'));

-- 登录日志测试数据（最近30天）
INSERT INTO login_log (user_id, username, login_ip, login_location, browser, os, status, msg, login_time) VALUES
    (1, 'root', '192.168.1.100', '本地', 'Chrome', 'Windows 10', 1, '登录成功', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
    (1, 'root', '192.168.1.100', '本地', 'Chrome', 'Windows 10', 1, '登录成功', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (2, 'president', '192.168.1.101', '本地', 'Firefox', 'macOS', 1, '登录成功', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
    (3, 'java_minister', '192.168.1.102', '本地', 'Firefox', 'macOS', 0, '密码错误', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
    (3, 'java_minister', '192.168.1.102', '本地', 'Firefox', 'macOS', 1, '登录成功', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
    (4, 'python_minister', '192.168.1.103', '本地', 'Chrome', 'Ubuntu', 1, '登录成功', DATE_SUB(NOW(), INTERVAL 5 HOUR)),
    (5, 'c_minister', '192.168.1.104', '本地', 'Edge', 'Windows 11', 1, '登录成功', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (9, 'java_member_01', '192.168.1.105', '本地', 'Chrome', 'Windows 10', 1, '登录成功', DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (11, 'python_member_01', '192.168.1.106', '本地', 'Safari', 'macOS', 1, '登录成功', DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (14, 'candidate_01', '192.168.1.107', '本地', 'Chrome', 'Android', 1, '登录成功', DATE_SUB(NOW(), INTERVAL 5 DAY)),
    (15, 'candidate_02', '192.168.1.108', '本地', 'Safari', 'iOS', 1, '登录成功', DATE_SUB(NOW(), INTERVAL 6 DAY));

-- 第三方账号绑定测试数据
INSERT INTO `oauth_account` (`user_id`, `provider`, `provider_user_id`, `nickname`, `avatar`) VALUES
    (1, 'github', 'github_root_123', 'root_github', 'https://avatars.githubusercontent.com/u/1'),
    (3, 'qq', 'qq_zhangwei_456', 'Java大师', 'https://q.qlogo.cn/g?b=qq&nk=123456'),
    (4, 'wx', 'wx_wangfang_789', 'Pythonista', 'https://wx.qlogo.cn/mmopen/xxx');


-- =============================================================================
-- 第三部分：内容服务 (content_db)
-- =============================================================================
USE `content_db`;

SET FOREIGN_KEY_CHECKS = 0;

-- 清空所有表数据
TRUNCATE TABLE `favorite_item`;
TRUNCATE TABLE `favorite_folder`;
TRUNCATE TABLE `audit_log`;
TRUNCATE TABLE `user_action`;
TRUNCATE TABLE `statistics`;
TRUNCATE TABLE `comment`;
TRUNCATE TABLE `tag_relation`;
TRUNCATE TABLE `tag`;
TRUNCATE TABLE `video`;
TRUNCATE TABLE `file`;
TRUNCATE TABLE `note`;
TRUNCATE TABLE `question`;
TRUNCATE TABLE `article`;
TRUNCATE TABLE `content`;
TRUNCATE TABLE `category`;

SET FOREIGN_KEY_CHECKS = 1;

-- 分类数据
INSERT INTO category (id, name, code, parent_id, status) VALUES
    (1, '技术', 'tech', NULL, 1),
    (2, '生活', 'life', NULL, 1),
    (3, 'Java', 'java', 1, 1),
    (4, 'Python', 'python', 1, 1),
    (5, '数据库', 'database', 1, 1),
    (6, '前端', 'frontend', 1, 1),
    (7, '学习笔记', 'notes', NULL, 1),
    (8, '问答交流', 'qa', NULL, 1);

-- 标签数据
INSERT INTO tag (id, name, description) VALUES
    (1, 'SpringBoot', 'Spring Boot框架相关'),
    (2, 'MySQL', 'MySQL数据库'),
    (3, 'Vue', 'Vue.js前端框架'),
    (4, 'React', 'React前端框架'),
    (5, 'Python', 'Python编程语言'),
    (6, '算法', '算法与数据结构'),
    (7, '面试', '面试经验分享'),
    (8, '项目实战', '实战项目经验');

-- 内容主表数据
-- type: 0=文章, 1=笔记, 2=提问, 3=文件, 4=视频
-- status: 0=草稿, 1=待审核, 2=已发布, 3=垃圾箱, 4=已删除
INSERT INTO content (id, title, type, description, cover_file_id, category_id, uploaded_by, status) VALUES
    (1, 'Spring Boot 入门教程', 0, '从零开始学习Spring Boot，包含完整的项目实战', 'f3e47e30cbc6423a03e6eadaa39957b3', 3, 3, 2),
    (2, 'MySQL优化实战笔记', 1, '记录MySQL性能优化的各种技巧和经验', 'eee2919bf89340d4d7f0a9cf242eaa03', 5, 3, 2),
    (3, '如何准备Java面试？', 2, '想请教一下各位大佬，Java面试应该如何准备？', NULL, 3, 9, 2),
    (4, 'Java设计模式PPT', 3, '23种设计模式详解PPT，适合初学者', 'f3e47e30cbc6423a03e6eadaa39957b3', 3, 3, 2),
    (5, 'Vue3项目实战视频', 4, '从零搭建Vue3+TypeScript项目', 'ecea91a9f2d3e60d28ca0b0cc25ebf49', 6, 7, 2),
    (6, 'Python数据分析入门', 0, 'Python数据分析基础教程，包含Pandas、NumPy等', 'eee2919bf89340d4d7f0a9cf242eaa03', 4, 4, 2),
    (7, '算法刷题笔记', 1, 'LeetCode刷题笔记，包含常见算法题解', 'f3e47e30cbc6423a03e6eadaa39957b3', 7, 5, 2),
    (8, '前端面试题汇总', 0, '前端面试常见问题及答案整理', 'ecea91a9f2d3e60d28ca0b0cc25ebf49', 6, 13, 2),
    (9, 'Redis缓存实战', 1, 'Redis在实际项目中的应用笔记', 'eee2919bf89340d4d7f0a9cf242eaa03', 5, 3, 2),
    (10, '如何学习数据结构？', 2, '数据结构学习路线求推荐', NULL, 8, 14, 2);

-- 文章详情
INSERT INTO article (content_id, content) VALUES
    (1, '# Spring Boot 入门教程\n\n## 1. 什么是Spring Boot\n\nSpring Boot是一个快速开发框架...\n\n## 2. 快速开始\n\n```java\n@SpringBootApplication\npublic class Application {\n    public static void main(String[] args) {\n        SpringApplication.run(Application.class, args);\n    }\n}\n```'),
    (6, '# Python数据分析入门\n\n## 1. 环境准备\n\n```python\nimport pandas as pd\nimport numpy as np\n```\n\n## 2. 数据读取\n\n使用Pandas读取CSV文件...'),
    (8, '# 前端面试题汇总\n\n## 1. HTML/CSS\n\n### 盒模型\n\n盒模型包括content、padding、border、margin...\n\n## 2. JavaScript\n\n### 闭包\n\n闭包是指有权访问另一个函数作用域中变量的函数...');

-- 笔记详情
INSERT INTO note (content_id, content, format) VALUES
    (2, '# MySQL优化笔记\n\n## 索引优化\n- 使用合适的索引\n- 避免索引失效\n\n## 查询优化\n- 避免SELECT *\n- 使用EXPLAIN分析', 'markdown'),
    (7, '# 算法刷题笔记\n\n## 数组\n- 两数之和\n- 三数之和\n\n## 链表\n- 反转链表\n- 合并链表', 'markdown'),
    (9, '# Redis缓存实战\n\n## 缓存策略\n- Cache Aside\n- Read Through\n- Write Through', 'markdown');

-- 问题详情
INSERT INTO question (content_id, content, answer_count, solved) VALUES
    (3, '我是一名大三学生，明年准备找Java开发的工作。想请教一下：\n1. Java面试一般会问哪些问题？\n2. 需要准备哪些项目经验？\n3. 有什么好的学习资源推荐？', 3, 1),
    (10, '最近在学习数据结构，感觉有点难。想问一下：\n1. 数据结构应该按什么顺序学习？\n2. 有什么好的练习方法？', 2, 0);

-- 文件详情
INSERT INTO file (content_id, file_id, download_count) VALUES
    (4, 'deb4bf4510279520f204d35d84bb9657', 156);

-- 视频详情
INSERT INTO video (content_id, video_file_id, play_count) VALUES
    (5, 'f41011d788c87d6716a08144da70c39f', 892);

-- 内容-标签关联
INSERT INTO tag_relation (content_id, tag_id) VALUES
    (1, 1), (1, 8),
    (2, 2),
    (3, 7),
    (4, 8),
    (5, 3), (5, 8),
    (6, 5),
    (7, 6),
    (8, 3), (8, 4), (8, 7),
    (9, 2),
    (10, 6);

-- 评论数据
-- status: 0=草稿, 1=待审核, 2=已发布, 3=已删除
INSERT INTO comment (id, content_id, user_id, parent_id, reply_to_user_id, content, like_count, status) VALUES
    (1, 1, 9, NULL, NULL, '写得很详细，对新手很友好！', 12, 2),
    (2, 1, 11, NULL, NULL, '期待更新更多内容', 5, 2),
    (3, 1, 12, 1, 9, '同意，确实很适合入门', 3, 2),
    (4, 3, 3, NULL, NULL, '建议先把Java基础打牢，然后学习Spring全家桶', 8, 2),
    (5, 3, 4, NULL, NULL, '多刷LeetCode，面试算法题很重要', 6, 2),
    (6, 3, 5, 4, 3, '补充一下，JVM和并发也是重点', 4, 2),
    (7, 5, 9, NULL, NULL, '视频质量很高，学到了很多', 15, 2),
    (8, 6, 12, NULL, NULL, 'Python入门很友好，感谢分享', 7, 2),
    (9, 10, 3, NULL, NULL, '建议从数组和链表开始，然后是树和图', 5, 2),
    (10, 10, 5, NULL, NULL, '推荐《算法导论》这本书', 3, 2);

-- 统计数据
INSERT INTO statistics (content_id, view_count, like_count, favorite_count, comment_count) VALUES
    (1, 1520, 89, 45, 3),
    (2, 890, 56, 32, 0),
    (3, 650, 23, 12, 3),
    (4, 420, 34, 28, 0),
    (5, 2100, 156, 89, 1),
    (6, 780, 45, 23, 1),
    (7, 560, 38, 19, 0),
    (8, 1200, 78, 56, 0),
    (9, 340, 21, 15, 0),
    (10, 280, 12, 8, 2);

-- 用户行为数据
-- target_type: 0=content, 1=comment
-- action_type: 0=view, 1=like, 2=favorite, 3=comment
-- status: 0=取消, 1=激活
INSERT INTO user_action (user_id, target_id, target_type, action_type, status) VALUES
    (9, 1, 0, 0, 1),  -- 用户9浏览内容1
    (9, 1, 0, 1, 1),  -- 用户9点赞内容1
    (9, 1, 0, 2, 1),  -- 用户9收藏内容1
    (11, 1, 0, 0, 1),
    (11, 1, 0, 1, 1),
    (12, 5, 0, 0, 1),
    (12, 5, 0, 1, 1),
    (12, 5, 0, 2, 1),
    (3, 6, 0, 0, 1),
    (3, 6, 0, 1, 1),
    (4, 1, 0, 0, 1),
    (5, 7, 0, 0, 1),
    (5, 7, 0, 2, 1);

-- 审核日志
-- target_type: 0=内容, 1=评论
-- from_status/to_status: 0=草稿, 1=待审核, 2=已通过, 3=已删除
INSERT INTO audit_log (target_id, target_type, from_status, to_status, audit_reason, audit_by) VALUES
    (1, 0, 1, 2, '内容质量高，审核通过', 1),
    (2, 0, 1, 2, '笔记内容详实，通过', 1),
    (3, 0, 1, 2, '问题描述清晰，通过', 1),
    (4, 0, 1, 2, '资源有价值，通过', 1),
    (5, 0, 1, 2, '视频质量好，通过', 1),
    (6, 0, 1, 2, '教程内容完整，通过', 1);

-- 收藏夹数据
INSERT INTO favorite_folder (id, user_id, name, description, is_default, is_public, sort_order, item_count) VALUES
    (1, 9, '默认收藏夹', '系统自动创建', 1, 0, 0, 2),
    (2, 9, '技术学习', '收藏的技术文章', 0, 1, 1, 1),
    (3, 11, '默认收藏夹', '系统自动创建', 1, 0, 0, 1),
    (4, 12, '默认收藏夹', '系统自动创建', 1, 0, 0, 2);

-- 收藏内容数据
INSERT INTO favorite_item (folder_id, user_id, content_id, note, sort_order, status) VALUES
    (1, 9, 1, 'Spring Boot入门必看', 0, 1),
    (1, 9, 5, 'Vue3学习资料', 1, 1),
    (2, 9, 2, 'MySQL优化参考', 0, 1),
    (3, 11, 1, NULL, 0, 1),
    (4, 12, 5, '前端学习', 0, 1),
    (4, 12, 7, '算法刷题', 1, 1);

 

-- =============================================================================
-- 第四部分：考勤服务 (attendance_db)
-- =============================================================================
USE `attendance_db`;

SET FOREIGN_KEY_CHECKS = 0;

-- 清空所有表数据
TRUNCATE TABLE `operation_log`;
TRUNCATE TABLE `attendance_statistics`;
TRUNCATE TABLE `attendance_anomaly`;
TRUNCATE TABLE `leave_request`;
TRUNCATE TABLE `attendance_record`;
TRUNCATE TABLE `attendance_rule`;

SET FOREIGN_KEY_CHECKS = 1;

-- 考勤规则数据
-- attendance_type: 0=日常考勤, 1=活动考勤
-- target_type: 0=全体, 1=部门, 2=个人
-- status: 0=禁用, 1=启用
INSERT INTO attendance_rule (
    id, name, description, attendance_type, related_id, target_type, target_ids,
    check_in_start_time, check_in_end_time, check_out_start_time, check_out_end_time,
    late_threshold_minutes, early_threshold_minutes, location_required,
    effective_date, expiry_date, status, priority, created_by, created_at, updated_at, deleted
) VALUES
    (1, '上午考勤', '上午时段考勤规则：08:00-12:00', 0, NULL, 0, NULL,
     '08:00:00', '09:00:00', '11:30:00', '12:00:00', 15, 15, 0,
     '2024-01-01', NULL, 1, 10, 1, NOW(), NOW(), 0),
    (2, '下午考勤', '下午时段考勤规则：12:00-18:00', 0, NULL, 0, NULL,
     '12:00:00', '15:00:00', '17:30:00', '18:00:00', 15, 15, 0,
     '2024-01-01', NULL, 1, 20, 1, NOW(), NOW(), 0),
    (3, '晚上考勤', '晚上时段考勤规则：19:00-22:00', 0, NULL, 0, NULL,
     '19:00:00', '19:30:00', '21:30:00', '22:00:00', 15, 15, 0,
     '2024-01-01', NULL, 1, 30, 1, NOW(), NOW(), 0);

-- 考勤记录数据（最近一周，包含多个用户多天的记录）
-- attendance_type: 0=日常考勤, 1=活动考勤
-- status: 0=进行中, 1=已完成, 2=异常
INSERT INTO attendance_record (
    user_id, attendance_type, related_id, check_in_time, check_out_time,
    check_in_location, check_out_location, duration_minutes, attendance_date,
    rule_id, status, remark, created_at, updated_at, deleted
) VALUES
    -- ========== 7天前 ==========
    (2, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 7 DAY) + INTERVAL 8 HOUR + INTERVAL 20 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 7 DAY) + INTERVAL 12 HOUR, '协会办公室', '协会办公室', 220, 
     DATE_SUB(CURDATE(), INTERVAL 7 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    (2, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 7 DAY) + INTERVAL 14 HOUR + INTERVAL 5 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 7 DAY) + INTERVAL 18 HOUR, '协会办公室', '协会办公室', 235, 
     DATE_SUB(CURDATE(), INTERVAL 7 DAY), 2, 1, '正常考勤', NOW(), NOW(), 0),
    (3, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 7 DAY) + INTERVAL 8 HOUR + INTERVAL 30 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 7 DAY) + INTERVAL 11 HOUR + INTERVAL 45 MINUTE, '协会办公室', '协会办公室', 195, 
     DATE_SUB(CURDATE(), INTERVAL 7 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    (4, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 7 DAY) + INTERVAL 8 HOUR + INTERVAL 25 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 7 DAY) + INTERVAL 12 HOUR, '协会办公室', '协会办公室', 215, 
     DATE_SUB(CURDATE(), INTERVAL 7 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    (9, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 7 DAY) + INTERVAL 8 HOUR + INTERVAL 40 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 7 DAY) + INTERVAL 11 HOUR + INTERVAL 55 MINUTE, '协会办公室', '协会办公室', 195, 
     DATE_SUB(CURDATE(), INTERVAL 7 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    (10, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 7 DAY) + INTERVAL 8 HOUR + INTERVAL 35 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 7 DAY) + INTERVAL 12 HOUR, '协会办公室', '协会办公室', 205, 
     DATE_SUB(CURDATE(), INTERVAL 7 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    
    -- ========== 6天前 ==========
    (2, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 8 HOUR + INTERVAL 15 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 12 HOUR, '协会办公室', '协会办公室', 225, 
     DATE_SUB(CURDATE(), INTERVAL 6 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    (2, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 14 HOUR + INTERVAL 10 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 18 HOUR, '协会办公室', '协会办公室', 230, 
     DATE_SUB(CURDATE(), INTERVAL 6 DAY), 2, 1, '正常考勤', NOW(), NOW(), 0),
    (3, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 8 HOUR + INTERVAL 50 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 11 HOUR + INTERVAL 50 MINUTE, '协会办公室', '协会办公室', 180, 
     DATE_SUB(CURDATE(), INTERVAL 6 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    (4, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 8 HOUR + INTERVAL 20 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 12 HOUR, '协会办公室', '协会办公室', 220, 
     DATE_SUB(CURDATE(), INTERVAL 6 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    (9, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 9 HOUR + INTERVAL 10 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 12 HOUR, '协会办公室', '协会办公室', 170, 
     DATE_SUB(CURDATE(), INTERVAL 6 DAY), 1, 2, '迟到10分钟', NOW(), NOW(), 0),
    (11, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 8 HOUR + INTERVAL 30 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 12 HOUR, '协会办公室', '协会办公室', 210, 
     DATE_SUB(CURDATE(), INTERVAL 6 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    
    -- ========== 5天前 ==========
    (2, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 8 HOUR + INTERVAL 25 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 12 HOUR, '协会办公室', '协会办公室', 215, 
     DATE_SUB(CURDATE(), INTERVAL 5 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    (2, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 14 HOUR + INTERVAL 15 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 18 HOUR, '协会办公室', '协会办公室', 225, 
     DATE_SUB(CURDATE(), INTERVAL 5 DAY), 2, 1, '正常考勤', NOW(), NOW(), 0),
    (3, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 8 HOUR + INTERVAL 35 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 11 HOUR + INTERVAL 50 MINUTE, '协会办公室', '协会办公室', 195, 
     DATE_SUB(CURDATE(), INTERVAL 5 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    (5, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 8 HOUR + INTERVAL 40 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 12 HOUR, '协会办公室', '协会办公室', 200, 
     DATE_SUB(CURDATE(), INTERVAL 5 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    (10, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 8 HOUR + INTERVAL 30 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 12 HOUR, '协会办公室', '协会办公室', 210, 
     DATE_SUB(CURDATE(), INTERVAL 5 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    
    -- ========== 4天前 ==========
    (2, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 8 HOUR + INTERVAL 20 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 12 HOUR, '协会办公室', '协会办公室', 220, 
     DATE_SUB(CURDATE(), INTERVAL 4 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    (2, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 14 HOUR + INTERVAL 10 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 18 HOUR, '协会办公室', '协会办公室', 230, 
     DATE_SUB(CURDATE(), INTERVAL 4 DAY), 2, 1, '正常考勤', NOW(), NOW(), 0),
    (3, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 8 HOUR + INTERVAL 45 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 11 HOUR + INTERVAL 55 MINUTE, '协会办公室', '协会办公室', 190, 
     DATE_SUB(CURDATE(), INTERVAL 4 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    (4, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 8 HOUR + INTERVAL 30 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 12 HOUR, '协会办公室', '协会办公室', 210, 
     DATE_SUB(CURDATE(), INTERVAL 4 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    (9, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 8 HOUR + INTERVAL 50 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 12 HOUR, '协会办公室', '协会办公室', 190, 
     DATE_SUB(CURDATE(), INTERVAL 4 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    (11, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 14 HOUR + INTERVAL 10 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 17 HOUR + INTERVAL 20 MINUTE, '协会办公室', '协会办公室', 190, 
     DATE_SUB(CURDATE(), INTERVAL 4 DAY), 2, 2, '早退10分钟', NOW(), NOW(), 0),
    
    -- ========== 3天前 ==========
    (2, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 8 HOUR + INTERVAL 15 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 12 HOUR, '协会办公室', '协会办公室', 225, 
     DATE_SUB(CURDATE(), INTERVAL 3 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    (2, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 14 HOUR + INTERVAL 5 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 18 HOUR, '协会办公室', '协会办公室', 235, 
     DATE_SUB(CURDATE(), INTERVAL 3 DAY), 2, 1, '正常考勤', NOW(), NOW(), 0),
    (3, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 8 HOUR + INTERVAL 40 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 11 HOUR + INTERVAL 50 MINUTE, '协会办公室', '协会办公室', 190, 
     DATE_SUB(CURDATE(), INTERVAL 3 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    (5, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 8 HOUR + INTERVAL 35 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 12 HOUR, '协会办公室', '协会办公室', 205, 
     DATE_SUB(CURDATE(), INTERVAL 3 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    (10, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 8 HOUR + INTERVAL 25 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 12 HOUR, '协会办公室', '协会办公室', 215, 
     DATE_SUB(CURDATE(), INTERVAL 3 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    
    -- ========== 2天前 ==========
    (2, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 8 HOUR + INTERVAL 30 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 12 HOUR, '协会办公室', '协会办公室', 210, 
     DATE_SUB(CURDATE(), INTERVAL 2 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    (2, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 14 HOUR + INTERVAL 10 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 18 HOUR, '协会办公室', '协会办公室', 230, 
     DATE_SUB(CURDATE(), INTERVAL 2 DAY), 2, 1, '正常考勤', NOW(), NOW(), 0),
    (3, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 8 HOUR + INTERVAL 50 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 11 HOUR + INTERVAL 45 MINUTE, '协会办公室', '协会办公室', 175, 
     DATE_SUB(CURDATE(), INTERVAL 2 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    (4, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 8 HOUR + INTERVAL 20 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 12 HOUR, '协会办公室', '协会办公室', 220, 
     DATE_SUB(CURDATE(), INTERVAL 2 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    (9, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 8 HOUR + INTERVAL 35 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 12 HOUR, '协会办公室', '协会办公室', 205, 
     DATE_SUB(CURDATE(), INTERVAL 2 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    (11, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 8 HOUR + INTERVAL 40 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 12 HOUR, '协会办公室', '协会办公室', 200, 
     DATE_SUB(CURDATE(), INTERVAL 2 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    
    -- ========== 1天前 ==========
    (2, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 8 HOUR + INTERVAL 30 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 12 HOUR, '协会办公室', '协会办公室', 210, 
     DATE_SUB(CURDATE(), INTERVAL 1 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    (2, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 14 HOUR + INTERVAL 10 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 18 HOUR, '协会办公室', '协会办公室', 230, 
     DATE_SUB(CURDATE(), INTERVAL 1 DAY), 2, 1, '正常考勤', NOW(), NOW(), 0),
    (3, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 8 HOUR + INTERVAL 45 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 11 HOUR + INTERVAL 50 MINUTE, '协会办公室', '协会办公室', 185, 
     DATE_SUB(CURDATE(), INTERVAL 1 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    (4, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 8 HOUR + INTERVAL 25 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 12 HOUR, '协会办公室', '协会办公室', 215, 
     DATE_SUB(CURDATE(), INTERVAL 1 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    (9, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 9 HOUR + INTERVAL 20 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 12 HOUR, '协会办公室', '协会办公室', 160, 
     DATE_SUB(CURDATE(), INTERVAL 1 DAY), 1, 2, '迟到20分钟', NOW(), NOW(), 0),
    (10, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 8 HOUR + INTERVAL 30 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 12 HOUR, '协会办公室', '协会办公室', 210, 
     DATE_SUB(CURDATE(), INTERVAL 1 DAY), 1, 1, '正常考勤', NOW(), NOW(), 0),
    (11, 0, NULL, DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 14 HOUR + INTERVAL 5 MINUTE, 
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 17 HOUR, '协会办公室', '协会办公室', 175, 
     DATE_SUB(CURDATE(), INTERVAL 1 DAY), 2, 2, '早退30分钟', NOW(), NOW(), 0),
    
    -- ========== 今天进行中的考勤 ==========
    (2, 0, NULL, CURDATE() + INTERVAL 8 HOUR + INTERVAL 25 MINUTE, NULL, 
     '协会办公室', NULL, NULL, CURDATE(), 1, 0, '进行中', NOW(), NOW(), 0),
    (3, 0, NULL, CURDATE() + INTERVAL 8 HOUR + INTERVAL 50 MINUTE, NULL, 
     '协会办公室', NULL, NULL, CURDATE(), 1, 0, '进行中', NOW(), NOW(), 0),
    (4, 0, NULL, CURDATE() + INTERVAL 8 HOUR + INTERVAL 20 MINUTE, NULL, 
     '协会办公室', NULL, NULL, CURDATE(), 1, 0, '进行中', NOW(), NOW(), 0),
    (5, 0, NULL, CURDATE() + INTERVAL 8 HOUR + INTERVAL 35 MINUTE, NULL, 
     '协会办公室', NULL, NULL, CURDATE(), 1, 0, '进行中', NOW(), NOW(), 0),
    (9, 0, NULL, CURDATE() + INTERVAL 8 HOUR + INTERVAL 40 MINUTE, NULL, 
     '协会办公室', NULL, NULL, CURDATE(), 1, 0, '进行中', NOW(), NOW(), 0),
    (10, 0, NULL, CURDATE() + INTERVAL 8 HOUR + INTERVAL 30 MINUTE, NULL, 
     '协会办公室', NULL, NULL, CURDATE(), 1, 0, '进行中', NOW(), NOW(), 0);

-- 考勤异常数据
-- anomaly_type: 0=迟到, 1=早退, 2=缺勤, 3=漏签到, 4=漏签退
-- resolved: 0=未处理, 1=已处理
INSERT INTO attendance_anomaly (
    attendance_record_id, user_id, anomaly_type, anomaly_date, duration_minutes,
    description, resolved, handler_id, handler_note, handled_at, suppressed_by_leave,
    created_at, updated_at, deleted
) VALUES
    (12, 9, 0, DATE_SUB(CURDATE(), INTERVAL 6 DAY), 10, '迟到10分钟', 0, NULL, NULL, NULL, NULL, NOW(), NOW(), 0),
    (29, 11, 1, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 10, '早退10分钟', 0, NULL, NULL, NULL, NULL, NOW(), NOW(), 0),
    (NULL, 12, 2, DATE_SUB(CURDATE(), INTERVAL 2 DAY), NULL, '全天缺勤', 1, 2, '已请假', NOW(), 1, NOW(), NOW(), 0),
    (43, 9, 0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 20, '迟到20分钟', 0, NULL, NULL, NULL, NULL, NOW(), NOW(), 0),
    (46, 11, 1, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 30, '早退30分钟', 0, NULL, NULL, NULL, NULL, NOW(), NOW(), 0);

-- 请假申请数据
-- leave_type: 0=病假, 1=事假
-- status: 0=待审核, 1=已批准, 2=已拒绝, 3=已撤销
INSERT INTO leave_request (
    user_id, leave_type, start_date, end_date, days, reason,
    status, approver_id, approval_note, approved_at, attachment_file_ids,
    created_at, updated_at, deleted
) VALUES
    (12, 0, DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_SUB(CURDATE(), INTERVAL 2 DAY), 1.0, 
     '身体不适，需要休息', 1, 2, '同意请假，注意休息', DATE_SUB(NOW(), INTERVAL 3 DAY), NULL, 
     DATE_SUB(NOW(), INTERVAL 3 DAY), NOW(), 0),
    (9, 1, DATE_ADD(CURDATE(), INTERVAL 3 DAY), DATE_ADD(CURDATE(), INTERVAL 4 DAY), 2.0, 
     '家中有事需要处理', 0, NULL, NULL, NULL, NULL, NOW(), NOW(), 0),
    (11, 0, DATE_ADD(CURDATE(), INTERVAL 7 DAY), DATE_ADD(CURDATE(), INTERVAL 8 DAY), 2.0, 
     '预约体检', 0, NULL, NULL, NULL, NULL, NOW(), NOW(), 0);

-- 考勤统计数据（当月）
INSERT INTO attendance_statistics (
    user_id, stat_month, total_days, actual_days, late_count, early_count,
    absence_count, leave_days, total_duration_minutes, attendance_rate,
    created_at, updated_at
) VALUES
    (2, DATE_FORMAT(CURDATE(), '%Y-%m'), 22, 21, 0, 0, 0, 0, 9450, 95.45, NOW(), NOW()),
    (3, DATE_FORMAT(CURDATE(), '%Y-%m'), 22, 20, 0, 0, 0, 0, 9000, 90.91, NOW(), NOW()),
    (4, DATE_FORMAT(CURDATE(), '%Y-%m'), 22, 19, 0, 0, 0, 0, 8550, 86.36, NOW(), NOW()),
    (5, DATE_FORMAT(CURDATE(), '%Y-%m'), 22, 18, 0, 0, 0, 0, 8100, 81.82, NOW(), NOW()),
    (9, DATE_FORMAT(CURDATE(), '%Y-%m'), 22, 17, 2, 0, 0, 0, 7650, 77.27, NOW(), NOW()),
    (10, DATE_FORMAT(CURDATE(), '%Y-%m'), 22, 17, 0, 0, 0, 0, 7650, 77.27, NOW(), NOW()),
    (11, DATE_FORMAT(CURDATE(), '%Y-%m'), 22, 16, 0, 2, 0, 0, 7200, 72.73, NOW(), NOW()),
    (12, DATE_FORMAT(CURDATE(), '%Y-%m'), 22, 15, 0, 0, 1, 1.0, 6750, 68.18, NOW(), NOW());

-- 操作日志数据
INSERT INTO operation_log (
    user_id, operation_type, operation_module, operation_desc,
    request_method, request_url, request_params, response_result,
    ip_address, user_agent, execution_time, status, error_message, created_at
) VALUES
    (2, 'APPROVE_LEAVE', 'LEAVE', '审批请假申请', 'POST', '/api/v1/attendance/leaves/1/approve', 
     '{"approved":true}', '{"code":200}', '192.168.1.101', 'Chrome', 125, 1, NULL, DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (9, 'CHECK_IN', 'ATTENDANCE', '签到', 'POST', '/api/v1/attendance/records/check-in', 
     '{"location":"协会办公室"}', '{"code":200}', '192.168.1.105', 'Chrome', 89, 1, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (9, 'CHECK_OUT', 'ATTENDANCE', '签退', 'POST', '/api/v1/attendance/records/check-out', 
     '{"location":"协会办公室"}', '{"code":200}', '192.168.1.105', 'Chrome', 76, 1, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (9, 'CREATE_LEAVE', 'LEAVE', '创建请假申请', 'POST', '/api/v1/attendance/leaves', 
     '{"leaveType":1,"days":2}', '{"code":200}', '192.168.1.105', 'Chrome', 156, 1, NULL, NOW());

1

-- =============================================================================
-- 第五部分：通知服务 (notification_db)
-- =============================================================================
USE `notification_db`;

-- 清空所有表数据
TRUNCATE TABLE `system_notification`;
TRUNCATE TABLE `email_log`;
TRUNCATE TABLE `sms_log`;

-- 系统通知数据
-- type: 1=SYSTEM系统通知, 2=ANNOUNCEMENT公告, 3=REMIND提醒
-- is_read: 0=未读, 1=已读
INSERT INTO system_notification (user_id, type, title, content, link_url, is_read) VALUES
    (1, 1, '系统初始化完成', '系统已成功初始化，欢迎使用！', '/dashboard', 1),
    (2, 1, '欢迎使用SIAE系统', '感谢您注册SIAE系统，祝您使用愉快！', '/dashboard', 1),
    (2, 2, '系统维护通知', '系统将于今晚22:00-24:00进行维护，请您提前保存数据。', NULL, 1),
    (3, 1, '欢迎使用SIAE系统', '感谢您注册SIAE系统，祝您使用愉快！', '/dashboard', 1),
    (3, 3, '您有新的待审核内容', '有用户提交了新的内容等待您审核', '/admin/audit', 0),
    (4, 1, '欢迎使用SIAE系统', '感谢您注册SIAE系统，祝您使用愉快！', '/dashboard', 1),
    (9, 1, '欢迎使用SIAE系统', '感谢您注册SIAE系统，祝您使用愉快！', '/dashboard', 1),
    (9, 3, '您的文章已通过审核', '您发布的文章《Spring Boot入门》已通过审核', '/content/1', 0),
    (9, 3, '您收到了新的评论', '有用户评论了您的文章', '/content/1#comments', 0),
    (11, 1, '欢迎使用SIAE系统', '感谢您注册SIAE系统，祝您使用愉快！', '/dashboard', 1),
    (12, 1, '欢迎使用SIAE系统', '感谢您注册SIAE系统，祝您使用愉快！', '/dashboard', 1),
    (14, 1, '欢迎加入SIAE', '您已成为SIAE候选成员，请积极参与协会活动！', '/member/guide', 0),
    (15, 1, '欢迎加入SIAE', '您已成为SIAE候选成员，请积极参与协会活动！', '/member/guide', 0);

-- 邮件发送记录
-- status: 0=PENDING待发送, 1=SUCCESS成功, 2=FAILED失败
INSERT INTO email_log (recipient, subject, content, status, error_msg, send_time) VALUES
    ('lihua@siae.com', '欢迎注册SIAE系统', '尊敬的用户，欢迎您注册SIAE系统...', 1, NULL, DATE_SUB(NOW(), INTERVAL 30 DAY)),
    ('zhangwei@siae.com', '欢迎注册SIAE系统', '尊敬的用户，欢迎您注册SIAE系统...', 1, NULL, DATE_SUB(NOW(), INTERVAL 25 DAY)),
    ('wangfang@siae.com', '欢迎注册SIAE系统', '尊敬的用户，欢迎您注册SIAE系统...', 1, NULL, DATE_SUB(NOW(), INTERVAL 25 DAY)),
    ('zhaomin@siae.com', '您的文章已通过审核', '您发布的文章已通过审核...', 1, NULL, DATE_SUB(NOW(), INTERVAL 5 DAY)),
    ('sunlin@siae.com', '密码重置验证码', '您的验证码是：123456，5分钟内有效', 1, NULL, DATE_SUB(NOW(), INTERVAL 3 DAY)),
    ('test@example.com', '测试邮件', '这是一封测试邮件', 2, 'Connection refused', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 短信发送记录（预留）
INSERT INTO sms_log (phone, content, template_code, status, error_msg, send_time) VALUES
    ('13800138001', '您的验证码是：123456，5分钟内有效', 'SMS_VERIFY_CODE', 1, NULL, DATE_SUB(NOW(), INTERVAL 10 DAY)),
    ('13800138002', '您的验证码是：654321，5分钟内有效', 'SMS_VERIFY_CODE', 1, NULL, DATE_SUB(NOW(), INTERVAL 5 DAY)),
    ('13800138005', '您有新的系统通知，请登录查看', 'SMS_NOTIFICATION', 1, NULL, DATE_SUB(NOW(), INTERVAL 2 DAY));


-- =============================================================================
-- 第六部分：媒体服务 (media_db)
-- =============================================================================
USE `media_db`;

SET FOREIGN_KEY_CHECKS = 0;

-- 清空所有表数据
TRUNCATE TABLE `audit_logs`;
TRUNCATE TABLE `multipart_parts`;
TRUNCATE TABLE `uploads`;
TRUNCATE TABLE `files`;

SET FOREIGN_KEY_CHECKS = 1;

-- 文件数据
-- access_policy: 'PUBLIC'=公开访问, 'PRIVATE'=私有访问
-- status: 'init'=初始化, 'uploading'=上传中, 'completed'=已完成, 'failed'=失败
INSERT INTO `files` (
    `id`, `tenant_id`, `owner_id`, `filename`, `bucket`, `storage_key`, 
    `size`, `mime`, `sha256`, `access_policy`, `status`, `biz_tags`
) VALUES
    -- 用户头像
    ('fbd9f8543888fb9370fc52c7397dcf1c', 'siae', '1', 'jo1.png', 'siae-media', 'siae/public/20251230/jo1.png', 
     489625, 'image/png', 'b257db963debcc6fc586390c0fc91955afcc31e21132d7df9eaa34421ea2414b', 'PUBLIC', 'completed', '{"type":"avatar","userId":1}'),
    ('f5ec3df8fedfa85d0a102b7cb5874da0', 'siae', '2', 'jo2.png', 'siae-media', 'siae/public/20251230/jo2.png', 
     696804, 'image/png', 'efec758637bba00b582dae6c458a56d3179cfcf0373bf8a46547788e9617cacd', 'PUBLIC', 'completed', '{"type":"avatar","userId":2}'),
    ('9237e3109ce88288e2232fdfa04bce55', 'siae', '3', 'jo3.png', 'siae-media', 'siae/public/20251230/jo3.png', 
     778747, 'image/png', '4d9b81925a3e08b831ff147c1a74627a8a87ebc5ced81fde7c7386caddb80c20', 'PUBLIC', 'completed', '{"type":"avatar","userId":3}'),
    ('7a376e75de1a08fc900d887f2f7ee08e', 'siae', '4', 'jo4.png', 'siae-media', 'siae/public/20251230/jo4.png', 
     586500, 'image/png', '2b98d57d3e43569f28064eab2fded86ce44fd1de4b586ce08b996271519a8eae', 'PUBLIC', 'completed', '{"type":"avatar","userId":4}'),
    ('a58cf68724d82a099037e2a17ff9eaa1', 'siae', '5', 'jo5.png', 'siae-media', 'siae/public/20251230/jo5.png', 
     587709, 'image/png', '6f6d49e57d7b6214a6c8b34c965a7fd19aa42141c1257ca44a20ccc663f4d0b3', 'PUBLIC', 'completed', '{"type":"avatar","userId":5}'),
    ('e7de8240512c5af52ef323a6584c10e3', 'siae', '6', 'Shinpei Ajiro.png', 'siae-media', 'siae/public/20251230/Shinpei Ajiro.png', 
     5030729, 'image/png', 'f427386396934d838edb760e69402257d42f236fdd983b8b19a69f01f7d77859', 'PUBLIC', 'completed', '{"type":"avatar","userId":6}'),
    ('0b26cd248846ea4d308f4eb6ee6f2c97', 'siae', '7', 'Ushio Kofune.png', 'siae-media', 'siae/public/20251230/Ushio Kofune.png', 
     6191123, 'image/png', 'ac5f5261280052ec45ea208f07fcdeef647fd3458b054d714c7e136a38c1d29b', 'PUBLIC', 'completed', '{"type":"avatar","userId":7}'),
    
    -- 背景图
    ('8613debb3214cbd8f6ff97104a45803a', 'siae', '1', 'forest.png', 'siae-media', 'siae/public/20251230/forest.png', 
     92262, 'image/png', '03857dbd6c6d2bea59e70b41db43815b85a0500038472f71933df466cb5d8f56', 'PUBLIC', 'completed', '{"type":"background","userId":1}'),
    ('c7dc85dd9e26283cc9fbf7a2478baa99', 'siae', '2', 'gad.png', 'siae-media', 'siae/public/20251230/gad.png', 
     127070, 'image/png', 'b21abd6f339a411494e2d5584534297b85d7f6eacab4b2943b69fad3bafdb6e6', 'PUBLIC', 'completed', '{"type":"background","userId":2}'),
    ('eb95710d2841d6d253bea36e7f45d453', 'siae', '3', 'ice.png', 'siae-media', 'siae/public/20251230/ice.png', 
     92888, 'image/png', '74e38c4acd0b50be4809dfb62770104b211e4dc2566780902273bedaa3d1b134', 'PUBLIC', 'completed', '{"type":"background","userId":3}'),
    ('04fbf782e74a17b03bbc93b6de89331f', 'siae', '4', 'mogudi.png', 'siae-media', 'siae/public/20251230/mogudi.png', 
     86259, 'image/png', 'e74b6483264f024302acf1f1bf055ee1cc1ddb50b81af42dbaa99a0468fb6bbb', 'PUBLIC', 'completed', '{"type":"background","userId":4}'),
    ('9dea73250f7f9233be7b9d1cbdb113c7', 'siae', '5', 'ocean.png', 'siae-media', 'siae/public/20251230/ocean.png', 
     39415, 'image/png', 'a66e6bf5733f90d01170a3ebdff754049fc8b96d12e106e00b13df85beb19dd8', 'PUBLIC', 'completed', '{"type":"background","userId":5}'),
    
    -- 成员大头照
    ('0b3537cfd9214887b032b75d1dd562a5', 'siae', '1', 'Steve.png', 'siae-media', 'siae/public/20251230/Steve.png', 
     3217138, 'image/png', 'b70ade943f0aedf3d74c987274f76db3df15597af23127a067098dd79dd714b2', 'PUBLIC', 'completed', '{"type":"headshot","userId":1}'),
    ('a5bfb3eb3e17ba5fbb0458d22e0435ca', 'siae', '2', 'Robin.png', 'siae-media', 'siae/public/20251230/Robin.png', 
     3164912, 'image/png', '351b5b26b0c845952758ff1a17643d2d70cacc88f914acaedb4d945f2ba91b89', 'PUBLIC', 'completed', '{"type":"headshot","userId":2}'),
    
    -- 内容封面（真实上传）
    ('f3e47e30cbc6423a03e6eadaa39957b3', 'siae', '2', 'cover-1.jpg', 'siae-media', 'siae/public/20251230/cover-1.jpg', 
     706712, 'image/jpeg', 'a5ee713cc6173b04567fb87c9b8c5f43240c6fc3839ad615b5718e09d87f8823', 'PUBLIC', 'completed', '{"type":"cover","contentId":1}'),
    ('eee2919bf89340d4d7f0a9cf242eaa03', 'siae', '2', 'cover-2.png', 'siae-media', 'siae/public/20251230/cover-2.png', 
     146929, 'image/png', 'bde4f04ab28f00e46ab1ad0fb158bd8262469420fdb9570c25b2898ddb5a5801', 'PUBLIC', 'completed', '{"type":"cover","contentId":2}'),
    ('ecea91a9f2d3e60d28ca0b0cc25ebf49', 'siae', '3', 'cover-3.jpg', 'siae-media', 'siae/public/20251230/cover-3.jpg', 
     780707, 'image/jpeg', '6a9f258b28f46e202b7d12eca90f6ca09c224c96418293209c7c8117fd806b30', 'PUBLIC', 'completed', '{"type":"cover","contentId":5}'),
    
    -- 文档资源（真实上传）
    ('deb4bf4510279520f204d35d84bb9657', 'siae', '2', 'file-1.pdf', 'siae-media', 'siae/public/20251230/file-1.pdf', 
     10601165, 'application/pdf', '8611f80a7d9c2d78a55317ae5d99234799e0d07bcfd95cbf213e9e62cef7742d', 'PRIVATE', 'completed', '{"type":"document","contentId":4}'),
    
    -- 视频资源（真实上传）
    ('f41011d788c87d6716a08144da70c39f', 'siae', '3', 'video-1.mp4', 'siae-media', 'siae/public/20251230/video-1.mp4', 
     2601956, 'video/mp4', '13aa16b66bbdc8e9c5d62ca62d01095033b12de3c4a7e2317744700d1adc18a6', 'PRIVATE', 'completed', '{"type":"video","contentId":5}'),
    
    -- 证书文件（使用真实上传的文件ID）
    ('56ccb8887a72f083ac355c3708840fbe', 'siae', '1', 'cert-lanqiao.jpg', 'certificates', 'certificates/2024/lanqiao.jpg', 
     135228, 'image/jpeg', '4d433a8537d4c583410dcb42d73c6eed45f03b63e8c80f23574f726005da665f', 'PRIVATE', 'completed', '{"type":"certificate","category":"lanqiao"}'),
    ('e5b94fb341133d3b39146818e9bf52a5', 'siae', '1', 'cert-2.png', 'certificates', 'certificates/2024/programming.png', 
     217293, 'image/png', '5d965c4a72f7261f8eca427e11d4a56f7321821737a7bdf82fa23f106c53990c', 'PRIVATE', 'completed', '{"type":"certificate","category":"programming"}'),
    ('d53e6790b14fec2d125c741d68ff2841', 'siae', '1', 'cert-校奖.jpg', 'certificates', 'certificates/2024/school.jpg', 
     104980, 'image/jpeg', 'c385349bb8ca0369667dfb003928180d60ddfea8574365c18d3991cdc1a2a0af', 'PRIVATE', 'completed', '{"type":"certificate","category":"school"}');

-- 审计日志
-- actor_type: 'service'=服务, 'user'=用户, 'system'=系统
-- action: 'init'=初始化上传, 'complete'=完成上传, 'download'=下载文件, 'delete'=删除文件, 'update_policy'=更新访问策略
INSERT INTO `audit_logs` (
    `file_id`, `tenant_id`, `actor_type`, `actor_id`, `action`, `ip`, `user_agent`, `metadata`
) VALUES
    ('fbd9f8543888fb9370fc52c7397dcf1c', 'siae', 'user', '1', 'init', '192.168.1.100', 'Chrome/120.0', '{"source":"profile"}'),
    ('fbd9f8543888fb9370fc52c7397dcf1c', 'siae', 'user', '1', 'complete', '192.168.1.100', 'Chrome/120.0', '{"duration":1500}'),
    ('cover-0001', 'siae', 'user', '2', 'init', '192.168.1.101', 'Firefox/121.0', '{"source":"content"}'),
    ('cover-0001', 'siae', 'user', '2', 'complete', '192.168.1.101', 'Firefox/121.0', '{"duration":2000}'),
    ('file-design-pattern-ppt', 'siae', 'user', '2', 'init', '192.168.1.101', 'Firefox/121.0', '{"source":"content"}'),
    ('file-design-pattern-ppt', 'siae', 'user', '2', 'complete', '192.168.1.101', 'Firefox/121.0', '{"duration":5000}'),
    ('file-design-pattern-ppt', 'siae', 'user', '5', 'download', '192.168.1.104', 'Chrome/120.0', '{}'),
    ('file-design-pattern-ppt', 'siae', 'user', '6', 'download', '192.168.1.105', 'Safari/17.0', '{}'),
    ('video-vue3-tutorial', 'siae', 'user', '3', 'init', '192.168.1.102', 'Chrome/120.0', '{"source":"content"}'),
    ('video-vue3-tutorial', 'siae', 'user', '3', 'complete', '192.168.1.102', 'Chrome/120.0', '{"duration":30000}');


-- =============================================================================
-- 第七部分：AI服务 (ai_db)
-- =============================================================================
USE `ai_db`;

-- 清空所有表数据
TRUNCATE TABLE `ai_chat_session`;

-- AI聊天会话数据
INSERT INTO `ai_chat_session` (`session_id`, `user_id`, `title`, `messages`) VALUES
    ('550e8400-e29b-41d4-a716-446655440001', 2, 'Java面试准备', 
     '[{"role":"user","content":"Java面试一般会问哪些问题？"},{"role":"assistant","content":"Java面试通常会涉及以下几个方面：\\n1. Java基础：集合框架、多线程、IO流等\\n2. JVM：内存模型、垃圾回收、类加载机制\\n3. 框架：Spring、MyBatis等\\n4. 数据库：SQL优化、索引原理\\n5. 分布式：微服务、消息队列、缓存等"}]'),
    
    ('550e8400-e29b-41d4-a716-446655440002', 3, 'Spring Boot配置问题', 
     '[{"role":"user","content":"Spring Boot如何配置多数据源？"},{"role":"assistant","content":"配置多数据源的步骤：\\n1. 在application.yml中配置多个数据源\\n2. 创建DataSource配置类\\n3. 使用@Primary标注主数据源\\n4. 配置对应的SqlSessionFactory和TransactionManager"}]'),
    
    ('550e8400-e29b-41d4-a716-446655440003', 4, 'Python学习路线', 
     '[{"role":"user","content":"Python数据分析应该学习哪些内容？"},{"role":"assistant","content":"Python数据分析学习路线：\\n1. Python基础语法\\n2. NumPy数值计算\\n3. Pandas数据处理\\n4. Matplotlib/Seaborn数据可视化\\n5. Scikit-learn机器学习入门"}]'),
    
    ('550e8400-e29b-41d4-a716-446655440004', 9, '算法学习', 
     '[{"role":"user","content":"如何高效刷LeetCode？"},{"role":"assistant","content":"高效刷LeetCode的建议：\\n1. 按专题刷题，如数组、链表、树等\\n2. 先理解题意，尝试自己思考\\n3. 学习最优解法，理解思路\\n4. 定期复习，总结模板\\n5. 坚持每天刷1-2题"}]'),
    
    ('550e8400-e29b-41d4-a716-446655440005', 11, '前端框架选择', 
     '[{"role":"user","content":"Vue和React应该学哪个？"},{"role":"assistant","content":"Vue和React各有优势：\\n\\nVue优点：\\n- 学习曲线平缓\\n- 中文文档完善\\n- 国内使用广泛\\n\\nReact优点：\\n- 生态系统丰富\\n- 大厂使用较多\\n- 函数式编程思想\\n\\n建议：如果是初学者，可以先学Vue；如果目标是大厂，建议学React。"}]');

-- =============================================================================
-- 数据初始化完成
-- =============================================================================

-- 显示各数据库表数据统计
SELECT '===== 测试数据初始化完成 =====' AS message;

SELECT 'user_db' AS `database`, 'user' AS `table`, COUNT(*) AS `count` FROM user_db.user
UNION ALL
SELECT 'user_db', 'membership', COUNT(*) FROM user_db.membership
UNION ALL
SELECT 'auth_db', 'user_role', COUNT(*) FROM auth_db.user_role
UNION ALL
SELECT 'auth_db', 'login_log', COUNT(*) FROM auth_db.login_log
UNION ALL
SELECT 'content_db', 'content', COUNT(*) FROM content_db.content
UNION ALL
SELECT 'content_db', 'comment', COUNT(*) FROM content_db.comment
UNION ALL
SELECT 'attendance_db', 'attendance_record', COUNT(*) FROM attendance_db.attendance_record
UNION ALL
SELECT 'attendance_db', 'leave_request', COUNT(*) FROM attendance_db.leave_request
UNION ALL
SELECT 'notification_db', 'system_notification', COUNT(*) FROM notification_db.system_notification
UNION ALL
SELECT 'media_db', 'files', COUNT(*) FROM media_db.files
UNION ALL
SELECT 'ai_db', 'ai_chat_session', COUNT(*) FROM ai_db.ai_chat_session;

-- =============================================================================
-- 测试账号说明
-- =============================================================================
-- 用户名              | 密码    | 角色        | 说明
-- root               | 123456  | ROLE_ROOT   | 超级管理员（系统管理员，非协会成员）
-- president          | 123456  | ROLE_ADMIN  | 会长
-- java_minister      | 123456  | ROLE_ADMIN  | Java部部长
-- python_minister    | 123456  | ROLE_ADMIN  | Python部部长
-- c_minister         | 123456  | ROLE_ADMIN  | C部部长
-- blockchain_minister| 123456  | ROLE_ADMIN  | 区块链部部长
-- web_minister       | 123456  | ROLE_ADMIN  | Web部部长
-- mobile_minister    | 123456  | ROLE_ADMIN  | 移动应用部部长
-- java_member_01     | 123456  | ROLE_MEMBER | Java部普通成员
-- java_member_02     | 123456  | ROLE_MEMBER | Java部普通成员
-- python_member_01   | 123456  | ROLE_MEMBER | Python部普通成员
-- c_member_01        | 123456  | ROLE_MEMBER | C部普通成员
-- web_member_01      | 123456  | ROLE_MEMBER | Web部普通成员
-- candidate_01       | 123456  | ROLE_USER   | 候选成员
-- candidate_02       | 123456  | ROLE_USER   | 候选成员
-- =============================================================================
