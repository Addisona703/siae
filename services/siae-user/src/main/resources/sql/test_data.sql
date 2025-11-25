-- =============================================================================
-- SIAE (软件协会官网) - 测试数据
-- 版本: 5.0
-- 描述: 用户数据库测试数据，可重复执行
-- 修改日期: 2025-11-09
-- 注意: 执行前会清空所有表数据，请谨慎使用！
-- =============================================================================

USE `user_db`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================================================
-- 清空所有表数据（保留表结构）
-- =============================================================================
TRUNCATE TABLE `user_award`;
TRUNCATE TABLE `member_position`;
TRUNCATE TABLE `member_department`;
TRUNCATE TABLE `membership`;
TRUNCATE TABLE `major_class_enrollment`;
TRUNCATE TABLE `user_profile`;
TRUNCATE TABLE `user`;
TRUNCATE TABLE `award_type`;
TRUNCATE TABLE `award_level`;
TRUNCATE TABLE `position`;
TRUNCATE TABLE `department`;
TRUNCATE TABLE `major`;

-- =============================================================================
-- 插入测试数据
-- =============================================================================

-- 专业数据
INSERT INTO `major` (`id`, `name`, `code`, `abbr`, `college_name`) VALUES
    (201, '软件技术', 'SE', '软件', '信息工程学院'),
    (202, '移动应用开发', 'MAD', '移动', '信息工程学院'),
    (203, '商务英语', 'BE', '商英', '外国语学院');

-- 部门数据
INSERT INTO `department` (`id`, `name`) VALUES
    (300, '主席团'),
    (301, 'JAVA部'),
    (302, 'Python部'),
    (303, 'C部'),
    (304, '区块链部'),
    (305, 'Web部'),
    (306, '移动应用部');

-- 职位数据
INSERT INTO `position` (`id`, `name`) VALUES
    (400, '会长'),
    (401, 'JAVA部部长'),
    (402, 'Python部部长'),
    (403, 'C部部长'),
    (404, '区块链部长'),
    (405, 'Web部长'),
    (406, '移动应用部长'),
    (499, '普通成员');

-- 奖项等级数据
INSERT INTO `award_level` (`id`, `name`) VALUES
    (501, '国家级'),
    (502, '省级'),
    (503, '校级');

-- 奖项类型数据
INSERT INTO `award_type` (`id`, `name`) VALUES
    (601, '学科竞赛'),
    (602, '创新创业'),
    (603, '文体艺术');

-- 用户数据
INSERT INTO `user` (`id`, `username`, `password`, `student_id`, `avatar_file_id`, `status`) VALUES
    (1, 'president', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2022010101', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (2, 'java_minister', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2022010102', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (3, 'python_minister', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2022010103', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (4, 'c_minister', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2022010104', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (5, 'java_member_01', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2023010101', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (6, 'python_member_01', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2023010102', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (7, 'c_member_01', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2023010103', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (8, 'candidate_01', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2023020101', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (9, 'candidate_02', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2023020202', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (10, 'normal_student', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2023030101', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (11, 'blockchain_minister', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2022010105', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (12, 'web_minister', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2022010106', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (13, 'mobile_minister', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2022010107', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (14, 'java_member_02', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2023010104', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (15, 'java_member_03', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2023010105', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (16, 'python_member_02', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2023010106', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (17, 'python_member_03', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2023010107', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (18, 'c_member_02', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2023010108', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (19, 'web_member_01', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2023010109', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (20, 'web_member_02', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2023010110', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (21, 'blockchain_member_01', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2023010111', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (22, 'mobile_member_01', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2023010112', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (23, 'java_member_04', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2024010101', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (24, 'java_member_05', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2024010102', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (25, 'python_member_04', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2024010103', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (26, 'c_member_03', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2024010104', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (27, 'web_member_03', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2024010105', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (28, 'blockchain_member_02', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2024010106', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (29, 'mobile_member_02', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2024010107', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (30, 'candidate_03', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2024020101', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (31, 'candidate_04', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2024020102', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (32, 'candidate_05', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2024020103', 'cb3c3c0ceecb49afafae3e9219117bee', 1),
    (33, 'python_member_05', '$2a$10$yQrPcv27mA7VUuB31ixewO5/OT.wy3ljOrohI3ezWgi9bBtb4G4Gi', '2024010108', 'cb3c3c0ceecb49afafae3e9219117bee', 1);

-- 用户详情数据
INSERT INTO `user_profile` (
    `user_id`, `nickname`, `real_name`, `background_file_id`,
    `bio`, `email`, `phone`, `gender`, `birthday`
) VALUES
    (1, '会长大人', '李华', 'cb3c3c0ceecb49afafae3e9219117bee', '协会的领航者', 'lihua@siae.com', '13800138001', 1, '2002-01-15'),
    (2, 'Java大师', '张伟', 'cb3c3c0ceecb49afafae3e9219117bee', 'Everything is an object.', 'zhangwei@siae.com', '13800138002', 1, '2003-03-10'),
    (3, 'Pythonista', '王芳', 'cb3c3c0ceecb49afafae3e9219117bee', '人生苦短，我用Python。', 'wangfang@siae.com', '13800138003', 2, '2003-05-20'),
    (4, 'C语言之父(自封)', '刘强', 'cb3c3c0ceecb49afafae3e9219117bee', '指针，指针，还是指针。', 'liuqiang@siae.com', '13800138004', 1, '2003-07-01'),
    (5, 'Java小兵', '赵敏', 'cb3c3c0ceecb49afafae3e9219117bee', '努力学习Java中...', 'zhaomin@siae.com', '13800138005', 2, '2004-09-01'),
    (6, 'Python新手', '孙琳', 'cb3c3c0ceecb49afafae3e9219117bee', 'print("Hello World")', 'sunlin@siae.com', '13800138006', 2, '2004-10-10'),
    (7, 'C-Learner', '周杰', 'cb3c3c0ceecb49afafae3e9219117bee', 'C语言爱好者', 'zhoujie@siae.com', '13800138007', 1, '2004-11-11'),
    (8, '小萌新A', '吴迪', 'cb3c3c0ceecb49afafae3e9219117bee', '希望加入JAVA部', 'wudi@siae.com', '13800138008', 1, '2005-01-20'),
    (9, '小萌新B', '郑雪', 'cb3c3c0ceecb49afafae3e9219117bee', '对C语言很感兴趣', 'zhengxue@siae.com', '13800138009', 2, '2005-02-28'),
    (10, '路人甲', '冯程', 'cb3c3c0ceecb49afafae3e9219117bee', '我只是一个普通学生', 'fengcheng@siae.com', '13800138010', 1, '2004-06-06'),
    (11, '链圈大佬', '陈链', 'cb3c3c0ceecb49afafae3e9219117bee', 'To the moon!', 'chenlian@siae.com', '13800138011', 1, '2003-04-05'),
    (12, '前端高手', '朱倩', 'cb3c3c0ceecb49afafae3e9219117bee', 'CSS是世界上最好的语言', 'zhuqian@siae.com', '13800138012', 2, '2003-08-15'),
    (13, 'App开发者', '蒋鑫', 'cb3c3c0ceecb49afafae3e9219117bee', 'Android & iOS', 'jiangxin@siae.com', '13800138013', 1, '2003-10-25'),
    (14, 'Java达人', '林晓', 'cb3c3c0ceecb49afafae3e9219117bee', 'Spring Boot专家', 'linxiao@siae.com', '13800138014', 2, '2004-03-12'),
    (15, 'Java忍者', '黄磊', 'cb3c3c0ceecb49afafae3e9219117bee', '代码重构狂魔', 'huanglei@siae.com', '13800138015', 1, '2004-05-08'),
    (16, 'Python爱好者', '徐静', 'cb3c3c0ceecb49afafae3e9219117bee', '数据分析师', 'xujing@siae.com', '13800138016', 2, '2004-07-22'),
    (17, 'AI研究员', '马云飞', 'cb3c3c0ceecb49afafae3e9219117bee', '深度学习爱好者', 'mayunfei@siae.com', '13800138017', 1, '2004-08-30'),
    (18, 'C++高手', '宋佳', 'cb3c3c0ceecb49afafae3e9219117bee', '算法竞赛选手', 'songjia@siae.com', '13800138018', 2, '2004-09-15'),
    (19, 'React开发', '谢明', 'cb3c3c0ceecb49afafae3e9219117bee', '全栈工程师', 'xieming@siae.com', '13800138019', 1, '2004-10-20'),
    (20, 'Vue专家', '袁梦', 'cb3c3c0ceecb49afafae3e9219117bee', '前端架构师', 'yuanmeng@siae.com', '13800138020', 2, '2004-11-05'),
    (21, '区块链开发', '韩冰', 'cb3c3c0ceecb49afafae3e9219117bee', 'Web3.0探索者', 'hanbing@siae.com', '13800138021', 1, '2004-12-10'),
    (22, 'Flutter开发', '唐雨', 'cb3c3c0ceecb49afafae3e9219117bee', '跨平台开发者', 'tangyu@siae.com', '13800138022', 2, '2005-01-15'),
    (23, 'Java新星', '贺强', 'cb3c3c0ceecb49afafae3e9219117bee', '微服务架构学习中', 'heqiang@siae.com', '13800138023', 1, '2005-03-20'),
    (24, 'SpringCloud', '秦丽', 'cb3c3c0ceecb49afafae3e9219117bee', '分布式系统爱好者', 'qinli@siae.com', '13800138024', 2, '2005-04-25'),
    (25, 'Django开发', '魏涛', 'cb3c3c0ceecb49afafae3e9219117bee', 'Python Web开发', 'weitao@siae.com', '13800138025', 1, '2005-05-30'),
    (26, '算法大神', '曹雪', 'cb3c3c0ceecb49afafae3e9219117bee', 'ACM金牌选手', 'caoxue@siae.com', '13800138026', 2, '2005-06-15'),
    (27, 'Node.js', '邓超', 'cb3c3c0ceecb49afafae3e9219117bee', '后端开发工程师', 'dengchao@siae.com', '13800138027', 1, '2005-07-10'),
    (28, 'Solidity', '叶琳', 'cb3c3c0ceecb49afafae3e9219117bee', '智能合约开发', 'yelin@siae.com', '13800138028', 2, '2005-08-05'),
    (29, 'iOS开发', '史杰', 'cb3c3c0ceecb49afafae3e9219117bee', 'Swift专家', 'shijie@siae.com', '13800138029', 1, '2005-09-01'),
    (30, '小白01', '田芳', 'cb3c3c0ceecb49afafae3e9219117bee', '刚入门编程', 'tianfang@siae.com', '13800138030', 2, '2005-10-10'),
    (31, '小白02', '姚明', 'cb3c3c0ceecb49afafae3e9219117bee', '对编程充满热情', 'yaoming@siae.com', '13800138031', 1, '2005-11-15'),
    (32, '小白03', '钱多多', 'cb3c3c0ceecb49afafae3e9219117bee', '想学Web开发', 'qianduoduo@siae.com', '13800138032', 2, '2005-12-20'),
    (33, 'AI工程师', '江雪', 'cb3c3c0ceecb49afafae3e9219117bee', '机器学习专家', 'jiangxue@siae.com', '13800138033', 2, '2005-02-28');

-- 班级关联数据
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
    (201, 2022, 1, 13, 1, 1),
    (201, 2023, 1, 14, 1, 1),
    (201, 2023, 1, 15, 1, 1),
    (201, 2023, 1, 16, 1, 1),
    (201, 2023, 1, 17, 1, 1),
    (201, 2023, 1, 18, 1, 1),
    (201, 2023, 1, 19, 1, 1),
    (201, 2023, 1, 20, 1, 1),
    (201, 2023, 1, 21, 1, 1),
    (202, 2023, 1, 22, 1, 1),
    (201, 2024, 1, 23, 1, 1),
    (201, 2024, 1, 24, 1, 1),
    (201, 2024, 1, 25, 1, 1),
    (201, 2024, 1, 26, 1, 1),
    (201, 2024, 1, 27, 1, 1),
    (201, 2024, 1, 28, 1, 1),
    (202, 2024, 1, 29, 1, 1),
    (201, 2024, 1, 30, 1, 1),
    (202, 2024, 1, 31, 1, 1),
    (203, 2024, 1, 32, 1, 1),
    (201, 2024, 1, 33, 1, 1);

-- 成员统一表数据（正式成员：lifecycle_status=1）
INSERT INTO `membership` (`user_id`, `headshot_file_id`, `lifecycle_status`, `join_date`) VALUES
    (1, 'cb3c3c0ceecb49afafae3e9219117bee', 1, '2022-09-01'),
    (2, 'cb3c3c0ceecb49afafae3e9219117bee', 1, '2022-09-10'),
    (3, 'cb3c3c0ceecb49afafae3e9219117bee', 1, '2022-09-10'),
    (4, 'cb3c3c0ceecb49afafae3e9219117bee', 1, '2022-09-10'),
    (5, 'cb3c3c0ceecb49afafae3e9219117bee', 1, '2023-09-15'),
    (6, 'cb3c3c0ceecb49afafae3e9219117bee', 1, '2023-09-15'),
    (7, 'cb3c3c0ceecb49afafae3e9219117bee', 1, '2023-09-15'),
    (11, 'cb3c3c0ceecb49afafae3e9219117bee', 1, '2022-09-10'),
    (12, 'cb3c3c0ceecb49afafae3e9219117bee', 1, '2022-09-10'),
    (13, 'cb3c3c0ceecb49afafae3e9219117bee', 1, '2022-09-10'),
    (14, 'cb3c3c0ceecb49afafae3e9219117bee', 1, '2023-10-01'),
    (15, 'cb3c3c0ceecb49afafae3e9219117bee', 1, '2023-10-05'),
    (16, 'cb3c3c0ceecb49afafae3e9219117bee', 1, '2023-10-10'),
    (17, 'cb3c3c0ceecb49afafae3e9219117bee', 1, '2023-10-15'),
    (18, 'cb3c3c0ceecb49afafae3e9219117bee', 1, '2023-10-20'),
    (19, 'cb3c3c0ceecb49afafae3e9219117bee', 1, '2023-11-01'),
    (20, 'cb3c3c0ceecb49afafae3e9219117bee', 1, '2023-11-05'),
    (21, 'cb3c3c0ceecb49afafae3e9219117bee', 1, '2023-11-10'),
    (22, 'cb3c3c0ceecb49afafae3e9219117bee', 1, '2023-11-15'),
    (23, 'cb3c3c0ceecb49afafae3e9219117bee', 1, '2024-09-01'),
    (24, 'cb3c3c0ceecb49afafae3e9219117bee', 1, '2024-09-05'),
    (25, 'cb3c3c0ceecb49afafae3e9219117bee', 1, '2024-09-10'),
    (26, 'cb3c3c0ceecb49afafae3e9219117bee', 1, '2024-09-15'),
    (27, 'cb3c3c0ceecb49afafae3e9219117bee', 1, '2024-09-20'),
    (28, 'cb3c3c0ceecb49afafae3e9219117bee', 1, '2024-09-25'),
    (29, 'cb3c3c0ceecb49afafae3e9219117bee', 1, '2024-10-01'),
    (33, 'cb3c3c0ceecb49afafae3e9219117bee', 1, '2024-10-10');

-- 候选成员数据（lifecycle_status=0）
INSERT INTO `membership` (`user_id`, `headshot_file_id`, `lifecycle_status`, `join_date`) VALUES
    (8, NULL, 0, NULL),
    (9, NULL, 0, NULL),
    (30, NULL, 0, NULL),
    (31, NULL, 0, NULL),
    (32, NULL, 0, NULL);

-- 部门归属数据
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
    (12, 301, '2023-10-05', 0),
    (13, 302, '2023-10-10', 0),
    (14, 302, '2023-10-15', 0),
    (15, 303, '2023-10-20', 0),
    (16, 305, '2023-11-01', 0),
    (17, 305, '2023-11-05', 0),
    (18, 304, '2023-11-10', 0),
    (19, 306, '2023-11-15', 0),
    (20, 301, '2024-09-01', 0),
    (21, 301, '2024-09-05', 0),
    (22, 302, '2024-09-10', 0),
    (23, 303, '2024-09-15', 0),
    (24, 305, '2024-09-20', 0),
    (25, 304, '2024-09-25', 0),
    (26, 306, '2024-10-01', 0),
    (27, 302, '2024-10-10', 0);

-- 职位记录数据
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
    (10, 406, 306, '2022-09-10', NULL),
    (11, 499, 301, '2023-10-01', NULL),
    (12, 499, 301, '2023-10-05', NULL),
    (13, 499, 302, '2023-10-10', NULL),
    (14, 499, 302, '2023-10-15', NULL),
    (15, 499, 303, '2023-10-20', NULL),
    (16, 499, 305, '2023-11-01', NULL),
    (17, 499, 305, '2023-11-05', NULL),
    (18, 499, 304, '2023-11-10', NULL),
    (19, 499, 306, '2023-11-15', NULL),
    (20, 499, 301, '2024-09-01', NULL),
    (21, 499, 301, '2024-09-05', NULL),
    (22, 499, 302, '2024-09-10', NULL),
    (23, 499, 303, '2024-09-15', NULL),
    (24, 499, 305, '2024-09-20', NULL),
    (25, 499, 304, '2024-09-25', NULL),
    (26, 499, 306, '2024-10-01', NULL),
    (27, 499, 302, '2024-10-10', NULL);

-- 获奖记录数据
INSERT INTO `user_award` (
    `award_title`, `award_level_id`, `award_type_id`,
    `awarded_by`, `awarded_at`, `certificate_file_id`, `team_members`
) VALUES
    -- 2024年11月-12月获奖记录
    ('全国大学生服务外包创新创业大赛一等奖', 501, 602, '教育部', '2024-11-20', 'certificate-file-0001', JSON_ARRAY(1, 2, 3, 5)),
    ('蓝桥杯软件类全国总决赛一等奖', 501, 601, '工业和信息化部人才交流中心', '2024-12-01', 'certificate-file-0002', JSON_ARRAY(1, 2, 6, 7)),
    ('中国大学生计算机设计大赛国家级一等奖', 501, 601, '教育部高等学校计算机类专业教学指导委员会', '2024-12-15', 'certificate-file-0003', JSON_ARRAY(2, 5, 14)),
    
    -- 2025年1月-2月获奖记录
    ('全国大学生数学建模竞赛国家一等奖', 501, 601, '中国工业与应用数学学会', '2025-01-10', 'certificate-file-0004', JSON_ARRAY(3, 6, 16)),
    ('中国"互联网+"大学生创新创业大赛国家铜奖', 501, 602, '教育部', '2025-02-15', 'certificate-file-0005', JSON_ARRAY(1, 11, 12, 13)),
    
    -- 省级获奖 - 2024年11月-12月
    ('蓝桥杯软件类省赛一等奖', 502, 601, '工业和信息化部人才交流中心', '2024-11-10', 'certificate-file-0006', JSON_ARRAY(2)),
    ('蓝桥杯软件类省赛一等奖', 502, 601, '工业和信息化部人才交流中心', '2024-11-10', 'certificate-file-0007', JSON_ARRAY(5)),
    ('蓝桥杯软件类省赛一等奖', 502, 601, '工业和信息化部人才交流中心', '2024-11-10', 'certificate-file-0008', JSON_ARRAY(14)),
    ('蓝桥杯软件类省赛二等奖', 502, 601, '工业和信息化部人才交流中心', '2024-11-10', 'certificate-file-0009', JSON_ARRAY(15)),
    ('省大学生程序设计竞赛一等奖', 502, 601, '省教育厅', '2024-11-20', 'certificate-file-0010', JSON_ARRAY(1, 2, 4)),
    ('省大学生程序设计竞赛二等奖', 502, 601, '省教育厅', '2024-11-20', 'certificate-file-0011', JSON_ARRAY(6, 7, 18)),
    ('省"互联网+"大学生创新创业大赛金奖', 502, 602, '省教育厅', '2024-12-15', 'certificate-file-0012', JSON_ARRAY(1, 3, 11, 12)),
    ('省"互联网+"大学生创新创业大赛银奖', 502, 602, '省教育厅', '2024-12-15', 'certificate-file-0013', JSON_ARRAY(13, 19, 20)),
    ('省大学生电子设计竞赛一等奖', 502, 601, '省教育厅', '2024-12-01', 'certificate-file-0014', JSON_ARRAY(4, 18, 26)),
    ('省大学生数学建模竞赛一等奖', 502, 601, '省教育厅', '2024-12-20', 'certificate-file-0015', JSON_ARRAY(3, 16)),
    ('省大学生数学建模竞赛二等奖', 502, 601, '省教育厅', '2024-12-20', 'certificate-file-0016', JSON_ARRAY(17, 25)),
    
    -- 2025年3月-5月获奖记录
    ('全国大学生软件测试大赛国家一等奖', 501, 601, '教育部', '2025-03-15', 'certificate-file-0017', JSON_ARRAY(2, 14, 15)),
    ('中国高校计算机大赛-团队程序设计天梯赛国家二等奖', 501, 601, '教育部高等学校计算机类专业教学指导委员会', '2025-04-20', 'certificate-file-0018', JSON_ARRAY(1, 2, 5, 14, 23)),
    ('全国大学生信息安全竞赛国家三等奖', 501, 601, '教育部高等学校网络空间安全专业教学指导委员会', '2025-05-10', 'certificate-file-0019', JSON_ARRAY(4, 7, 18)),
    
    -- 省级获奖 - 2025年3月-5月
    ('蓝桥杯软件类省赛一等奖', 502, 601, '工业和信息化部人才交流中心', '2025-03-12', 'certificate-file-0021', JSON_ARRAY(23)),
    ('蓝桥杯软件类省赛一等奖', 502, 601, '工业和信息化部人才交流中心', '2025-03-12', 'certificate-file-0022', JSON_ARRAY(24)),
    ('蓝桥杯软件类省赛二等奖', 502, 601, '工业和信息化部人才交流中心', '2025-03-12', 'certificate-file-0023', JSON_ARRAY(25)),
    ('蓝桥杯软件类省赛二等奖', 502, 601, '工业和信息化部人才交流中心', '2025-03-12', 'certificate-file-0024', JSON_ARRAY(26)),
    ('省大学生程序设计竞赛一等奖', 502, 601, '省教育厅', '2025-04-18', 'certificate-file-0025', JSON_ARRAY(2, 14, 23)),
    ('省大学生程序设计竞赛二等奖', 502, 601, '省教育厅', '2025-04-18', 'certificate-file-0026', JSON_ARRAY(15, 24, 26)),
    ('省"互联网+"大学生创新创业大赛金奖', 502, 602, '省教育厅', '2025-05-20', 'certificate-file-0027', JSON_ARRAY(1, 11, 21, 28)),
    ('省"互联网+"大学生创新创业大赛银奖', 502, 602, '省教育厅', '2025-05-20', 'certificate-file-0028', JSON_ARRAY(12, 19, 27)),
    
    -- 2025年6月-8月获奖记录
    ('"挑战杯"全国大学生课外学术科技作品竞赛国家三等奖', 501, 602, '共青团中央', '2025-06-20', 'certificate-file-0020', JSON_ARRAY(1, 3, 6, 16, 17)),
    ('省大学生人工智能创新大赛一等奖', 502, 601, '省教育厅', '2025-07-15', 'certificate-file-0029', JSON_ARRAY(3, 16, 17, 33)),
    ('省大学生移动应用开发大赛一等奖', 502, 601, '省教育厅', '2025-08-10', 'certificate-file-0030', JSON_ARRAY(13, 22, 29)),
    
    -- 校级获奖 - 2024年11月-2025年
    ('校程序设计大赛一等奖', 503, 601, '校团委', '2024-11-15', 'certificate-file-0031', JSON_ARRAY(2)),
    ('校程序设计大赛一等奖', 503, 601, '校团委', '2024-11-15', 'certificate-file-0032', JSON_ARRAY(5)),
    ('校程序设计大赛二等奖', 503, 601, '校团委', '2024-11-15', 'certificate-file-0033', JSON_ARRAY(14)),
    ('校程序设计大赛二等奖', 503, 601, '校团委', '2024-11-15', 'certificate-file-0034', JSON_ARRAY(15)),
    ('校程序设计大赛三等奖', 503, 601, '校团委', '2024-11-15', 'certificate-file-0035', JSON_ARRAY(6)),
    ('校创新创业大赛一等奖', 503, 602, '校团委', '2025-01-20', 'certificate-file-0036', JSON_ARRAY(1, 11, 21)),
    ('校创新创业大赛二等奖', 503, 602, '校团委', '2025-01-20', 'certificate-file-0037', JSON_ARRAY(12, 19)),
    ('校程序设计大赛一等奖', 503, 601, '校团委', '2025-09-10', 'certificate-file-0038', JSON_ARRAY(23)),
    ('校程序设计大赛一等奖', 503, 601, '校团委', '2025-09-10', 'certificate-file-0039', JSON_ARRAY(24)),
    ('校程序设计大赛二等奖', 503, 601, '校团委', '2025-09-10', 'certificate-file-0040', JSON_ARRAY(25)),
    ('校程序设计大赛二等奖', 503, 601, '校团委', '2025-09-10', 'certificate-file-0041', JSON_ARRAY(26)),
    ('校程序设计大赛三等奖', 503, 601, '校团委', '2025-09-10', 'certificate-file-0042', JSON_ARRAY(27)),
    ('校文化艺术节最佳创意奖', 503, 603, '校团委', '2025-10-01', 'certificate-file-0043', JSON_ARRAY(12, 20)),
    ('校运动会团体第三名', 503, 603, '校体育部', '2025-10-15', 'certificate-file-0044', JSON_ARRAY(1, 2, 3, 4, 5, 6, 7));

SET FOREIGN_KEY_CHECKS = 1;
COMMIT;
