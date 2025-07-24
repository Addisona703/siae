-- =================================================================
-- 数据库：auth_db
-- 描述：创建认证授权核心数据库
-- =================================================================
DROP DATABASE IF EXISTS `auth_db`;
CREATE DATABASE IF NOT EXISTS `auth_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `auth_db`;

-- =================================================================
-- 表：role (角色表)
-- 描述：存储系统中的所有角色定义
-- =================================================================
CREATE TABLE `role` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
  `name` VARCHAR(64) NOT NULL COMMENT '角色名称',
  `code` VARCHAR(64) NOT NULL COMMENT '角色编码, 用于程序判断',
  `description` VARCHAR(255) COMMENT '角色描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- =================================================================
-- 表：permission (权限表)
-- 描述：存储系统中所有的权限点，形成权限树
-- =================================================================
CREATE TABLE `permission` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '权限ID',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父权限ID, NULL表示顶级菜单',
  `name` VARCHAR(64) NOT NULL COMMENT '权限名称',
  `code` VARCHAR(100) NOT NULL COMMENT '权限编码, e.g., "sys:user:add"',
  `type` VARCHAR(32) NOT NULL COMMENT '权限类型：menu菜单、button按钮',
  `path` VARCHAR(255) COMMENT '路由地址 (当type为menu时)',
  `component` VARCHAR(255) COMMENT '组件路径 (当type为menu时)',
  `icon` VARCHAR(64) COMMENT '菜单图标 (当type为menu时)',
  `sort_order` INT DEFAULT 0 COMMENT '排序值, 值越小越靠前',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_code` (`code`),
  INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- =================================================================
-- 表：user_role (用户角色关联表)
-- 描述：存储用户与角色的多对多关系
-- =================================================================
CREATE TABLE `user_role` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID (关联user_db.user.id)',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- =================================================================
-- 表：role_permission (角色权限关联表)
-- 描述：存储角色与权限的多对多关系
-- =================================================================
CREATE TABLE `role_permission` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT NOT NULL COMMENT '权限ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
  CONSTRAINT `fk_role_permission_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_role_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- =================================================================
-- 表：user_permission (用户权限关联表)
-- 描述：存储用户与权限的多对多关系，用于直接给用户授予特定权限
-- =================================================================
CREATE TABLE `user_permission` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID (关联user_db.user.id)',
  `permission_id` BIGINT NOT NULL COMMENT '权限ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY `uk_user_permission` (`user_id`, `permission_id`),
  CONSTRAINT `fk_user_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户权限关联表';

-- =================================================================
-- 表：user_auth (用户认证表)
-- 描述：存储用户的认证令牌信息，用于支持刷新和注销
-- =================================================================
CREATE TABLE `user_auth` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '认证ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `access_token` VARCHAR(1024) NOT NULL COMMENT '访问令牌',
  `refresh_token` VARCHAR(1024) NOT NULL COMMENT '刷新令牌',
  `token_type` VARCHAR(32) DEFAULT 'Bearer' COMMENT '令牌类型',
  `expires_at` DATETIME NOT NULL COMMENT '访问令牌过期时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_access_token` (`access_token`(255)),
  INDEX `idx_refresh_token` (`refresh_token`(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户认证表';

-- =================================================================
-- 表：login_log (登录日志表)
-- 描述：记录用户的登录历史
-- =================================================================
CREATE TABLE `login_log` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '访问ID',
  `user_id` BIGINT COMMENT '用户ID (登录成功时记录)',
  `username` VARCHAR(64) NOT NULL COMMENT '登录账号',
  `login_ip` VARCHAR(64) DEFAULT '' COMMENT '登录IP',
  `login_location` VARCHAR(255) DEFAULT '' COMMENT '登录地点',
  `browser` VARCHAR(50) DEFAULT '' COMMENT '浏览器类型',
  `os` VARCHAR(50) DEFAULT '' COMMENT '操作系统',
  `status` TINYINT DEFAULT 0 COMMENT '登录状态（0失败 1成功）',
  `msg` VARCHAR(255) DEFAULT '' COMMENT '提示消息',
  `login_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_login_time` (`login_time`),
  INDEX `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统访问记录';


-- =================================================================
-- 初始化数据
-- =================================================================

-- 初始化角色数据
INSERT INTO `role` (`name`, `code`, `description`) VALUES
('超级管理员', 'ROLE_ROOT', '系统超级管理员，拥有所有权限'),
('管理员', 'ROLE_ADMIN', '系统管理员，拥有大部分管理权限'),
('协会成员', 'ROLE_MEMBER', '软件协会成员'),
('普通用户', 'ROLE_USER', '普通注册用户');

-- 初始化权限数据
INSERT INTO `permission` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `component`, `icon`, `sort_order`) VALUES

-- =================================================================
-- 系统管理服务模块 (auth-service)
-- 提供系统基础的用户认证、权限管理等功能
-- =================================================================
(1, '系统管理', 'system:auth:manage', 'menu', NULL, '/system', 'Layout', 'system', 1),

-- 用户管理
(2, '用户管理', 'auth:user:manage', 'menu', 1, 'user', 'system/user/index', 'user', 1),
(3, '用户查询', 'auth:user:query', 'button', 2, NULL, NULL, NULL, 1),
(4, '用户新增', 'auth:user:add', 'button', 2, NULL, NULL, NULL, 2),
(5, '用户修改', 'auth:user:edit', 'button', 2, NULL, NULL, NULL, 3),
(6, '用户删除', 'auth:user:delete', 'button', 2, NULL, NULL, NULL, 4),

-- 角色管理
(7, '角色管理', 'auth:role:manage', 'menu', 1, 'role', 'system/role/index', 'peoples', 2),
(8, '角色查询', 'auth:role:query', 'button', 7, NULL, NULL, NULL, 1),
(9, '角色新增', 'auth:role:add', 'button', 7, NULL, NULL, NULL, 2),
(10, '角色修改', 'auth:role:edit', 'button', 7, NULL, NULL, NULL, 3),
(11, '角色删除', 'auth:role:delete', 'button', 7, NULL, NULL, NULL, 4),

-- 权限管理
(12, '权限管理', 'auth:permission:manage', 'menu', 1, 'permission', 'system/permission/index', 'tree-table', 3),
(13, '权限查询', 'auth:permission:query', 'button', 12, NULL, NULL, NULL, 1),
(14, '权限新增', 'auth:permission:add', 'button', 12, NULL, NULL, NULL, 2),
(15, '权限修改', 'auth:permission:edit', 'button', 12, NULL, NULL, NULL, 3),
(16, '权限删除', 'auth:permission:delete', 'button', 12, NULL, NULL, NULL, 4),

-- 日志管理
(98, '日志管理', 'auth:log:manage', 'menu', 1, 'log', 'system/log/index', 'log', 4),
(99, '日志查询', 'auth:log:query', 'button', 98, NULL, NULL, NULL, 1),
(100, '日志导出', 'auth:log:export', 'button', 98, NULL, NULL, NULL, 2),

-- 用户角色关联管理
(101, '用户角色关联', 'auth:user:role:manage', 'menu', 1, 'user-role', 'system/user-role/index', 'link', 5),
(102, '分配用户角色', 'auth:user:role:assign', 'button', 101, NULL, NULL, NULL, 1),
(103, '查询用户角色', 'auth:user:role:query', 'button', 101, NULL, NULL, NULL, 2),
(104, '移除用户角色', 'auth:user:role:remove', 'button', 101, NULL, NULL, NULL, 3),

-- 用户权限关联管理
(105, '用户权限关联', 'auth:user:permission:manage', 'menu', 1, 'user-permission', 'system/user-permission/index', 'permission', 6),
(106, '分配用户权限', 'auth:user:permission:assign', 'button', 105, NULL, NULL, NULL, 1),
(107, '查询用户权限', 'auth:user:permission:query', 'button', 105, NULL, NULL, NULL, 2),
(108, '移除用户权限', 'auth:user:permission:remove', 'button', 105, NULL, NULL, NULL, 3),

-- =================================================================
-- 内容管理服务模块 (content-service)
-- 提供内容发布、分类管理、标签管理等功能
-- =================================================================
(17, '内容管理', 'system:content:manage', 'menu', NULL, '/content', 'Layout', 'documentation', 2),

-- 内容管理子菜单
(18, '内容管理', 'content:manage', 'menu', 17, 'content', 'content/content/index', 'edit', 1),
(19, '内容发布', 'content:publish', 'button', 18, NULL, NULL, NULL, 1),
(20, '内容编辑', 'content:edit', 'button', 18, NULL, NULL, NULL, 2),
(21, '内容删除', 'content:delete', 'button', 18, NULL, NULL, NULL, 3),
(22, '内容查询', 'content:query', 'button', 18, NULL, NULL, NULL, 4),
(23, '内容列表查看', 'content:list:view', 'button', 18, NULL, NULL, NULL, 5),
(24, '热门内容查看', 'content:hot:view', 'button', 18, NULL, NULL, NULL, 6),

-- 分类管理子菜单
(25, '分类管理', 'content:category:manage', 'menu', 17, 'category', 'content/category/index', 'tree', 2),
(26, '分类创建', 'content:category:create', 'button', 25, NULL, NULL, NULL, 1),
(27, '分类编辑', 'content:category:edit', 'button', 25, NULL, NULL, NULL, 2),
(28, '分类删除', 'content:category:delete', 'button', 25, NULL, NULL, NULL, 3),
(29, '分类查看', 'content:category:view', 'button', 25, NULL, NULL, NULL, 4),
(30, '分类状态切换', 'content:category:toggle', 'button', 25, NULL, NULL, NULL, 5),

-- 标签管理子菜单
(31, '标签管理', 'content:tag:manage', 'menu', 17, 'tag', 'content/tag/index', 'tag', 3),
(32, '标签创建', 'content:tag:create', 'button', 31, NULL, NULL, NULL, 1),
(33, '标签编辑', 'content:tag:edit', 'button', 31, NULL, NULL, NULL, 2),
(34, '标签删除', 'content:tag:delete', 'button', 31, NULL, NULL, NULL, 3),
(35, '标签查看', 'content:tag:view', 'button', 31, NULL, NULL, NULL, 4),

-- 用户交互管理子菜单
(36, '用户交互', 'content:interaction:manage', 'menu', 17, 'interaction', 'content/interaction/index', 'user', 4),
(37, '交互记录', 'content:interaction:record', 'button', 36, NULL, NULL, NULL, 1),
(38, '交互取消', 'content:interaction:cancel', 'button', 36, NULL, NULL, NULL, 2),

-- 统计管理子菜单
(39, '统计管理', 'content:statistics:manage', 'menu', 17, 'statistics', 'content/statistics/index', 'chart', 5),
(40, '统计查看', 'content:statistics:view', 'button', 39, NULL, NULL, NULL, 1),
(41, '统计更新', 'content:statistics:update', 'button', 39, NULL, NULL, NULL, 2),

-- 审核管理子菜单
(42, '审核管理', 'content:audit:manage', 'menu', 17, 'audit', 'content/audit/index', 'audit', 6),
(43, '审核处理', 'content:audit:handle', 'button', 42, NULL, NULL, NULL, 1),
(44, '审核查看', 'content:audit:view', 'button', 42, NULL, NULL, NULL, 2),
(45, '审核通过', 'content:audit:approve', 'button', 42, NULL, NULL, NULL, 3),
(46, '审核拒绝', 'content:audit:reject', 'button', 42, NULL, NULL, NULL, 4),

-- 评论管理子菜单
(47, '评论管理', 'content:comment:manage', 'menu', 17, 'comment', 'content/comment/index', 'message', 7),
(48, '评论创建', 'content:comment:create', 'button', 47, NULL, NULL, NULL, 1),
(49, '评论编辑', 'content:comment:edit', 'button', 47, NULL, NULL, NULL, 2),
(50, '评论删除', 'content:comment:delete', 'button', 47, NULL, NULL, NULL, 3),
(51, '评论查看', 'content:comment:view', 'button', 47, NULL, NULL, NULL, 4),

-- =================================================================
-- 用户管理服务模块 (user-service)
-- 提供用户信息管理、成员管理、班级奖项管理等功能
-- =================================================================
(52, '用户管理', 'system:user:manage', 'menu', NULL, '/user', 'Layout', 'peoples', 3),

-- 用户信息管理子菜单
(53, '用户信息', 'user:manage', 'menu', 52, 'user', 'user/user/index', 'user', 1),
(54, '用户创建', 'user:create', 'button', 53, NULL, NULL, NULL, 1),
(55, '用户更新', 'user:update', 'button', 53, NULL, NULL, NULL, 2),
(56, '用户删除', 'user:delete', 'button', 53, NULL, NULL, NULL, 3),
(57, '用户查看', 'user:view', 'button', 53, NULL, NULL, NULL, 4),
(58, '用户列表', 'user:list', 'button', 53, NULL, NULL, NULL, 5),

-- 用户详情管理子菜单
(59, '用户详情', 'user:profile:manage', 'menu', 52, 'profile', 'user/profile/index', 'profile', 2),
(60, '详情创建', 'user:profile:create', 'button', 59, NULL, NULL, NULL, 1),
(61, '详情更新', 'user:profile:update', 'button', 59, NULL, NULL, NULL, 2),
(62, '详情删除', 'user:profile:delete', 'button', 59, NULL, NULL, NULL, 3),
(63, '详情查看', 'user:profile:view', 'button', 59, NULL, NULL, NULL, 4),

-- 正式成员管理子菜单
(64, '正式成员', 'user:member:manage', 'menu', 52, 'member', 'user/member/index', 'team', 3),
(65, '成员更新', 'user:member:update', 'button', 64, NULL, NULL, NULL, 1),
(66, '成员查看', 'user:member:view', 'button', 64, NULL, NULL, NULL, 2),
(67, '成员列表', 'user:member:list', 'button', 64, NULL, NULL, NULL, 3),

-- 候选成员管理子菜单
(68, '候选成员', 'user:candidate:manage', 'menu', 52, 'candidate', 'user/candidate/index', 'user-plus', 4),
(69, '候选成员创建', 'user:candidate:create', 'button', 68, NULL, NULL, NULL, 1),
(70, '候选成员更新', 'user:candidate:update', 'button', 68, NULL, NULL, NULL, 2),
(71, '候选成员删除', 'user:candidate:delete', 'button', 68, NULL, NULL, NULL, 3),
(72, '候选成员查看', 'user:candidate:view', 'button', 68, NULL, NULL, NULL, 4),
(73, '候选成员列表', 'user:candidate:list', 'button', 68, NULL, NULL, NULL, 5),

-- 班级管理子菜单
(74, '班级管理', 'user:class:manage', 'menu', 52, 'class', 'user/class/index', 'school', 5),
(75, '班级创建', 'user:class:create', 'button', 74, NULL, NULL, NULL, 1),
(76, '班级更新', 'user:class:update', 'button', 74, NULL, NULL, NULL, 2),
(77, '班级删除', 'user:class:delete', 'button', 74, NULL, NULL, NULL, 3),
(78, '班级查看', 'user:class:view', 'button', 74, NULL, NULL, NULL, 4),
(79, '班级列表', 'user:class:list', 'button', 74, NULL, NULL, NULL, 5),

-- 奖项类型管理子菜单
(80, '奖项类型', 'user:award-type:manage', 'menu', 52, 'award-type', 'user/award-type/index', 'trophy', 6),
(81, '奖项类型创建', 'user:award-type:create', 'button', 80, NULL, NULL, NULL, 1),
(82, '奖项类型更新', 'user:award-type:update', 'button', 80, NULL, NULL, NULL, 2),
(83, '奖项类型删除', 'user:award-type:delete', 'button', 80, NULL, NULL, NULL, 3),
(84, '奖项类型查看', 'user:award-type:view', 'button', 80, NULL, NULL, NULL, 4),
(85, '奖项类型列表', 'user:award-type:list', 'button', 80, NULL, NULL, NULL, 5),

-- 奖项等级管理子菜单
(86, '奖项等级', 'user:award-level:manage', 'menu', 52, 'award-level', 'user/award-level/index', 'star', 7),
(87, '奖项等级创建', 'user:award-level:create', 'button', 86, NULL, NULL, NULL, 1),
(88, '奖项等级更新', 'user:award-level:update', 'button', 86, NULL, NULL, NULL, 2),
(89, '奖项等级删除', 'user:award-level:delete', 'button', 86, NULL, NULL, NULL, 3),
(90, '奖项等级查看', 'user:award-level:view', 'button', 86, NULL, NULL, NULL, 4),
(91, '奖项等级列表', 'user:award-level:list', 'button', 86, NULL, NULL, NULL, 5),

-- 用户获奖记录管理子菜单
(109, '获奖记录', 'user:award:manage', 'menu', 52, 'award', 'user/award/index', 'medal', 8),
(110, '获奖记录创建', 'user:award:create', 'button', 109, NULL, NULL, NULL, 1),
(111, '获奖记录更新', 'user:award:update', 'button', 109, NULL, NULL, NULL, 2),
(112, '获奖记录删除', 'user:award:delete', 'button', 109, NULL, NULL, NULL, 3),
(113, '获奖记录查看', 'user:award:view', 'button', 109, NULL, NULL, NULL, 4),
(114, '获奖记录列表', 'user:award:list', 'button', 109, NULL, NULL, NULL, 5);

-- 初始化角色-权限关联关系
-- 超级管理员(ROLE_ROOT)拥有所有权限
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT (SELECT id FROM `role` WHERE code = 'ROLE_ROOT'), id FROM `permission`;

-- 管理员(ROLE_ADMIN)拥有大部分管理权限 (排除高危删除权限)
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT (SELECT id FROM `role` WHERE code = 'ROLE_ADMIN'), id FROM `permission`
WHERE code NOT IN (
    'auth:permission:delete',
    'auth:role:delete',
    'auth:user:delete',
    'content:delete',
    'content:category:delete',
    'content:tag:delete',
    'content:comment:delete',
    'user:delete',
    'user:profile:delete',
    'user:candidate:delete',
    'user:class:delete',
    'user:award-type:delete',
    'user:award-level:delete',
    'user:award:delete'
);

-- 协会成员(ROLE_MEMBER)拥有内容相关的基础权限和用户基础权限
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT (SELECT id FROM `role` WHERE code = 'ROLE_MEMBER'), id FROM `permission`
WHERE code IN (
    -- 内容管理菜单权限
    'content:manage',
    'content:content:manage',
    'content:category:manage',
    'content:tag:manage',
    'content:interaction:manage',
    'content:statistics:manage',
    'content:comment:manage',
    -- 内容基础操作权限
    'content:publish',
    'content:edit',
    'content:query',
    'content:list:view',
    'content:hot:view',
    -- 分类查看权限
    'content:category:view',
    -- 标签查看权限
    'content:tag:view',
    -- 用户交互权限
    'content:interaction:record',
    'content:interaction:cancel',
    -- 统计查看权限
    'content:statistics:view',
    -- 评论基础权限
    'content:comment:create',
    'content:comment:edit',
    'content:comment:view',
    -- 用户管理菜单权限
    'user:manage',
    'user:user:manage',
    'user:profile:manage',
    'user:member:manage',
    'user:candidate:manage',
    'user:class:manage',
    'user:award-type:manage',
    'user:award-level:manage',
    'user:award:manage',
    -- 用户基础操作权限
    'user:view',
    'user:list',
    'user:profile:view',
    'user:profile:update',
    'user:member:view',
    'user:member:list',
    'user:candidate:view',
    'user:candidate:list',
    'user:class:view',
    'user:class:list',
    'user:award-type:view',
    'user:award-type:list',
    'user:award-level:view',
    'user:award-level:list',
    'user:award:view',
    'user:award:list'
);

-- 普通用户(ROLE_USER)拥有基础的内容查看和交互权限，以及基础的用户信息查看权限
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT (SELECT id FROM `role` WHERE code = 'ROLE_USER'), id FROM `permission`
WHERE code IN (
    -- 内容查看权限
    'content:query',
    'content:list:view',
    'content:hot:view',
    -- 分类查看权限
    'content:category:view',
    -- 标签查看权限
    'content:tag:view',
    -- 用户交互权限
    'content:interaction:record',
    'content:interaction:cancel',
    -- 统计查看权限
    'content:statistics:view',
    -- 评论基础权限
    'content:comment:create',
    'content:comment:view',
    -- 用户基础查看权限
    'user:profile:view',
    'user:member:view',
    'user:member:list',
    'user:class:view',
    'user:class:list',
    'user:award-type:view',
    'user:award-type:list',
    'user:award-level:view',
    'user:award-level:list',
    'user:award:view',
    'user:award:list'
);

-- 初始化用户角色数据
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES
(1, (SELECT id FROM `role` WHERE code = 'ROLE_ROOT')),
(2, (SELECT id FROM `role` WHERE code = 'ROLE_ADMIN')),
(3, (SELECT id FROM `role` WHERE code = 'ROLE_MEMBER')),
(4, (SELECT id FROM `role` WHERE code = 'ROLE_USER'));
