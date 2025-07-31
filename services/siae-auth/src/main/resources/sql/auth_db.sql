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
-- 认证服务模块 (siae-auth)
-- 提供系统基础的用户认证、权限管理等功能
-- 基于 AuthPermissions.java 中定义的权限常量
-- =================================================================
(1, '认证管理', 'auth:manage', 'menu', NULL, '/auth', 'Layout', 'system', 1),

-- 角色管理
(2, '角色管理', 'auth:role:manage', 'menu', 1, 'role', 'auth/role/index', 'peoples', 1),
(3, '角色查询', 'auth:role:query', 'button', 2, NULL, NULL, NULL, 1),
(4, '角色新增', 'auth:role:add', 'button', 2, NULL, NULL, NULL, 2),
(5, '角色修改', 'auth:role:edit', 'button', 2, NULL, NULL, NULL, 3),
(6, '角色删除', 'auth:role:delete', 'button', 2, NULL, NULL, NULL, 4),

-- 权限管理
(7, '权限管理', 'auth:permission:manage', 'menu', 1, 'permission', 'auth/permission/index', 'tree-table', 2),
(8, '权限查询', 'auth:permission:query', 'button', 7, NULL, NULL, NULL, 1),
(9, '权限新增', 'auth:permission:add', 'button', 7, NULL, NULL, NULL, 2),
(10, '权限修改', 'auth:permission:edit', 'button', 7, NULL, NULL, NULL, 3),
(11, '权限删除', 'auth:permission:delete', 'button', 7, NULL, NULL, NULL, 4),

-- 日志管理
(12, '日志管理', 'auth:log:manage', 'menu', 1, 'log', 'auth/log/index', 'log', 3),
(13, '日志查询', 'auth:log:query', 'button', 12, NULL, NULL, NULL, 1),
(14, '日志导出', 'auth:log:export', 'button', 12, NULL, NULL, NULL, 2),

-- 用户角色关联管理
(15, '用户角色管理', 'auth:user:role:manage', 'menu', 1, 'user-role', 'auth/user-role/index', 'link', 4),
(16, '分配用户角色', 'auth:user:role:assign', 'button', 15, NULL, NULL, NULL, 1),
(17, '查询用户角色', 'auth:user:role:query', 'button', 15, NULL, NULL, NULL, 2),
(18, '更新用户角色', 'auth:user:role:update', 'button', 15, NULL, NULL, NULL, 3),
(19, '移除用户角色', 'auth:user:role:remove', 'button', 15, NULL, NULL, NULL, 4),

-- 用户权限关联管理
(20, '用户权限管理', 'auth:user:permission:manage', 'menu', 1, 'user-permission', 'auth/user-permission/index', 'permission', 5),
(21, '分配用户权限', 'auth:user:permission:assign', 'button', 20, NULL, NULL, NULL, 1),
(22, '查询用户权限', 'auth:user:permission:query', 'button', 20, NULL, NULL, NULL, 2),
(23, '移除用户权限', 'auth:user:permission:remove', 'button', 20, NULL, NULL, NULL, 3),

-- =================================================================
-- 内容管理服务模块 (content-service)
-- 提供内容发布、分类管理、标签管理等功能
-- =================================================================
(50, '内容管理', 'system:content:manage', 'menu', NULL, '/content', 'Layout', 'documentation', 2),

-- 内容管理子菜单
(51, '内容管理', 'content:manage', 'menu', 50, 'content', 'content/content/index', 'edit', 1),
(52, '内容发布', 'content:publish', 'button', 51, NULL, NULL, NULL, 1),
(53, '内容编辑', 'content:edit', 'button', 51, NULL, NULL, NULL, 2),
(54, '内容删除', 'content:delete', 'button', 51, NULL, NULL, NULL, 3),
(55, '内容查询', 'content:query', 'button', 51, NULL, NULL, NULL, 4),
(56, '内容列表查看', 'content:list:view', 'button', 51, NULL, NULL, NULL, 5),
(57, '热门内容查看', 'content:hot:view', 'button', 51, NULL, NULL, NULL, 6),

-- 分类管理子菜单
(58, '分类管理', 'content:category:manage', 'menu', 50, 'category', 'content/category/index', 'tree', 2),
(59, '分类创建', 'content:category:create', 'button', 58, NULL, NULL, NULL, 1),
(60, '分类编辑', 'content:category:edit', 'button', 58, NULL, NULL, NULL, 2),
(61, '分类删除', 'content:category:delete', 'button', 58, NULL, NULL, NULL, 3),
(62, '分类查看', 'content:category:view', 'button', 58, NULL, NULL, NULL, 4),
(63, '分类状态切换', 'content:category:toggle', 'button', 58, NULL, NULL, NULL, 5),

-- 标签管理子菜单
(64, '标签管理', 'content:tag:manage', 'menu', 50, 'tag', 'content/tag/index', 'tag', 3),
(65, '标签创建', 'content:tag:create', 'button', 64, NULL, NULL, NULL, 1),
(66, '标签编辑', 'content:tag:edit', 'button', 64, NULL, NULL, NULL, 2),
(67, '标签删除', 'content:tag:delete', 'button', 64, NULL, NULL, NULL, 3),
(68, '标签查看', 'content:tag:view', 'button', 64, NULL, NULL, NULL, 4),

-- 用户交互管理子菜单
(69, '用户交互', 'content:interaction:manage', 'menu', 50, 'interaction', 'content/interaction/index', 'user', 4),
(70, '交互记录', 'content:interaction:record', 'button', 69, NULL, NULL, NULL, 1),
(71, '交互取消', 'content:interaction:cancel', 'button', 69, NULL, NULL, NULL, 2),

-- 统计管理子菜单
(72, '统计管理', 'content:statistics:manage', 'menu', 50, 'statistics', 'content/statistics/index', 'chart', 5),
(73, '统计查看', 'content:statistics:view', 'button', 72, NULL, NULL, NULL, 1),
(74, '统计更新', 'content:statistics:update', 'button', 72, NULL, NULL, NULL, 2),

-- 审核管理子菜单
(75, '审核管理', 'content:audit:manage', 'menu', 50, 'audit', 'content/audit/index', 'audit', 6),
(76, '审核处理', 'content:audit:handle', 'button', 75, NULL, NULL, NULL, 1),
(77, '审核查看', 'content:audit:view', 'button', 75, NULL, NULL, NULL, 2),
(78, '审核通过', 'content:audit:approve', 'button', 75, NULL, NULL, NULL, 3),
(79, '审核拒绝', 'content:audit:reject', 'button', 75, NULL, NULL, NULL, 4),

-- 评论管理子菜单
(80, '评论管理', 'content:comment:manage', 'menu', 50, 'comment', 'content/comment/index', 'message', 7),
(81, '评论创建', 'content:comment:create', 'button', 80, NULL, NULL, NULL, 1),
(82, '评论编辑', 'content:comment:edit', 'button', 80, NULL, NULL, NULL, 2),
(83, '评论删除', 'content:comment:delete', 'button', 80, NULL, NULL, NULL, 3),
(84, '评论查看', 'content:comment:view', 'button', 80, NULL, NULL, NULL, 4),

-- =================================================================
-- 用户管理服务模块 (user-service)
-- 提供用户信息管理、成员管理、班级奖项管理等功能
-- =================================================================
(100, '用户管理', 'system:user:manage', 'menu', NULL, '/user', 'Layout', 'peoples', 3),

-- 用户信息管理子菜单
(101, '用户信息', 'user:manage', 'menu', 100, 'user', 'user/user/index', 'user', 1),
(102, '用户创建', 'user:create', 'button', 101, NULL, NULL, NULL, 1),
(103, '用户更新', 'user:update', 'button', 101, NULL, NULL, NULL, 2),
(104, '用户删除', 'user:delete', 'button', 101, NULL, NULL, NULL, 3),
(105, '用户查看', 'user:view', 'button', 101, NULL, NULL, NULL, 4),
(106, '用户列表', 'user:list', 'button', 101, NULL, NULL, NULL, 5),

-- 用户详情管理子菜单
(107, '用户详情', 'user:profile:manage', 'menu', 100, 'profile', 'user/profile/index', 'profile', 2),
(108, '详情创建', 'user:profile:create', 'button', 107, NULL, NULL, NULL, 1),
(109, '详情更新', 'user:profile:update', 'button', 107, NULL, NULL, NULL, 2),
(110, '详情删除', 'user:profile:delete', 'button', 107, NULL, NULL, NULL, 3),
(111, '详情查看', 'user:profile:view', 'button', 107, NULL, NULL, NULL, 4),

-- 正式成员管理子菜单
(112, '正式成员', 'user:member:manage', 'menu', 100, 'member', 'user/member/index', 'team', 3),
(113, '成员创建', 'user:member:create', 'button', 112, NULL, NULL, NULL, 1),
(114, '成员更新', 'user:member:update', 'button', 112, NULL, NULL, NULL, 2),
(115, '成员删除', 'user:member:delete', 'button', 112, NULL, NULL, NULL, 3),
(116, '成员查看', 'user:member:view', 'button', 112, NULL, NULL, NULL, 4),
(117, '成员列表', 'user:member:list', 'button', 112, NULL, NULL, NULL, 5),

-- 候选成员管理子菜单
(118, '候选成员', 'user:candidate:manage', 'menu', 100, 'candidate', 'user/candidate/index', 'user-plus', 4),
(119, '候选成员创建', 'user:candidate:create', 'button', 118, NULL, NULL, NULL, 1),
(120, '候选成员更新', 'user:candidate:update', 'button', 118, NULL, NULL, NULL, 2),
(121, '候选成员删除', 'user:candidate:delete', 'button', 118, NULL, NULL, NULL, 3),
(122, '候选成员查看', 'user:candidate:view', 'button', 118, NULL, NULL, NULL, 4),
(123, '候选成员列表', 'user:candidate:list', 'button', 118, NULL, NULL, NULL, 5),

-- 班级管理子菜单
(124, '班级管理', 'user:class:manage', 'menu', 100, 'class', 'user/class/index', 'school', 5),
(125, '班级创建', 'user:class:create', 'button', 124, NULL, NULL, NULL, 1),
(126, '班级更新', 'user:class:update', 'button', 124, NULL, NULL, NULL, 2),
(127, '班级删除', 'user:class:delete', 'button', 124, NULL, NULL, NULL, 3),
(128, '班级查看', 'user:class:view', 'button', 124, NULL, NULL, NULL, 4),
(129, '班级列表', 'user:class:list', 'button', 124, NULL, NULL, NULL, 5),

-- 奖项类型管理子菜单
(130, '奖项类型', 'user:award-type:manage', 'menu', 100, 'award-type', 'user/award-type/index', 'trophy', 6),
(131, '奖项类型创建', 'user:award-type:create', 'button', 130, NULL, NULL, NULL, 1),
(132, '奖项类型更新', 'user:award-type:update', 'button', 130, NULL, NULL, NULL, 2),
(133, '奖项类型删除', 'user:award-type:delete', 'button', 130, NULL, NULL, NULL, 3),
(134, '奖项类型查看', 'user:award-type:view', 'button', 130, NULL, NULL, NULL, 4),
(135, '奖项类型列表', 'user:award-type:list', 'button', 130, NULL, NULL, NULL, 5),

-- 奖项等级管理子菜单
(136, '奖项等级', 'user:award-level:manage', 'menu', 100, 'award-level', 'user/award-level/index', 'star', 7),
(137, '奖项等级创建', 'user:award-level:create', 'button', 136, NULL, NULL, NULL, 1),
(138, '奖项等级更新', 'user:award-level:update', 'button', 136, NULL, NULL, NULL, 2),
(139, '奖项等级删除', 'user:award-level:delete', 'button', 136, NULL, NULL, NULL, 3),
(140, '奖项等级查看', 'user:award-level:view', 'button', 136, NULL, NULL, NULL, 4),
(141, '奖项等级列表', 'user:award-level:list', 'button', 136, NULL, NULL, NULL, 5),

-- 用户获奖记录管理子菜单
(142, '获奖记录', 'user:award:manage', 'menu', 100, 'award', 'user/award/index', 'medal', 8),
(143, '获奖记录创建', 'user:award:create', 'button', 142, NULL, NULL, NULL, 1),
(144, '获奖记录更新', 'user:award:update', 'button', 142, NULL, NULL, NULL, 2),
(145, '获奖记录删除', 'user:award:delete', 'button', 142, NULL, NULL, NULL, 3),
(146, '获奖记录查看', 'user:award:view', 'button', 142, NULL, NULL, NULL, 4),
(147, '获奖记录列表', 'user:award:list', 'button', 142, NULL, NULL, NULL, 5);

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
    'content:delete',
    'content:category:delete',
    'content:tag:delete',
    'content:comment:delete',
    'user:delete',
    'user:profile:delete',
    'user:candidate:delete',
    'user:member:delete',
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
