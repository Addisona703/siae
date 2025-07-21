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
  `access_token` VARCHAR(512) NOT NULL COMMENT '访问令牌',
  `refresh_token` VARCHAR(512) NOT NULL COMMENT '刷新令牌',
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

-- 初始化权限数据 (采用提供的设计)
INSERT INTO `permission` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `component`, `icon`, `sort_order`) VALUES
-- 系统管理
(1, '系统管理', 'system:manage', 'menu', NULL, '/system', 'Layout', 'system', 1),
-- 用户管理
(2, '用户管理', 'system:user:manage', 'menu', 1, 'user', 'system/user/index', 'user', 1),
(3, '用户查询', 'system:user:query', 'button', 2, '', '', '', 1),
(4, '用户新增', 'system:user:add', 'button', 2, '', '', '', 2),
(5, '用户修改', 'system:user:edit', 'button', 2, '', '', '', 3),
(6, '用户删除', 'system:user:delete', 'button', 2, '', '', '', 4),
-- 角色管理
(7, '角色管理', 'system:role:manage', 'menu', 1, 'role', 'system/role/index', 'peoples', 2),
(8, '角色查询', 'system:role:query', 'button', 7, '', '', '', 1),
(9, '角色新增', 'system:role:add', 'button', 7, '', '', '', 2),
(10, '角色修改', 'system:role:edit', 'button', 7, '', '', '', 3),
(11, '角色删除', 'system:role:delete', 'button', 7, '', '', '', 4),
-- 权限管理
(12, '权限管理', 'system:permission:manage', 'menu', 1, 'permission', 'system/permission/index', 'tree-table', 3),
(13, '权限查询', 'system:permission:query', 'button', 12, '', '', '', 1),
(14, '权限新增', 'system:permission:add', 'button', 12, '', '', '', 2),
(15, '权限修改', 'system:permission:edit', 'button', 12, '', '', '', 3),
(16, '权限删除', 'system:permission:delete', 'button', 12, '', '', '', 4),

-- ==================== 内容管理模块 ====================
-- 内容管理主菜单
(17, '内容管理', 'content:manage', 'menu', NULL, '/content', 'Layout', 'documentation', 2),

-- 内容管理子菜单
(18, '内容管理', 'content:content:manage', 'menu', 17, 'content', 'content/content/index', 'edit', 1),
(19, '内容发布', 'system:content:publish', 'button', 18, '', '', '', 1),
(20, '内容编辑', 'system:content:edit', 'button', 18, '', '', '', 2),
(21, '内容删除', 'system:content:delete', 'button', 18, '', '', '', 3),
(22, '内容查询', 'system:content:query', 'button', 18, '', '', '', 4),
(23, '内容列表查看', 'content:list:view', 'button', 18, '', '', '', 5),
(24, '热门内容查看', 'content:hot:view', 'button', 18, '', '', '', 6),

-- 分类管理子菜单
(25, '分类管理', 'content:category:manage', 'menu', 17, 'category', 'content/category/index', 'tree', 2),
(26, '分类创建', 'content:category:create', 'button', 25, '', '', '', 1),
(27, '分类编辑', 'content:category:edit', 'button', 25, '', '', '', 2),
(28, '分类删除', 'content:category:delete', 'button', 25, '', '', '', 3),
(29, '分类查看', 'content:category:view', 'button', 25, '', '', '', 4),
(30, '分类状态切换', 'content:category:toggle', 'button', 25, '', '', '', 5),

-- 标签管理子菜单
(31, '标签管理', 'content:tag:manage', 'menu', 17, 'tag', 'content/tag/index', 'tag', 3),
(32, '标签创建', 'content:tag:create', 'button', 31, '', '', '', 1),
(33, '标签编辑', 'content:tag:edit', 'button', 31, '', '', '', 2),
(34, '标签删除', 'content:tag:delete', 'button', 31, '', '', '', 3),
(35, '标签查看', 'content:tag:view', 'button', 31, '', '', '', 4),

-- 用户交互管理子菜单
(36, '用户交互', 'content:interaction:manage', 'menu', 17, 'interaction', 'content/interaction/index', 'user', 4),
(37, '交互记录', 'content:interaction:record', 'button', 36, '', '', '', 1),
(38, '交互取消', 'content:interaction:cancel', 'button', 36, '', '', '', 2),

-- 统计管理子菜单
(39, '统计管理', 'content:statistics:manage', 'menu', 17, 'statistics', 'content/statistics/index', 'chart', 5),
(40, '统计查看', 'content:statistics:view', 'button', 39, '', '', '', 1),
(41, '统计更新', 'content:statistics:update', 'button', 39, '', '', '', 2),

-- 审核管理子菜单
(42, '审核管理', 'content:audit:manage', 'menu', 17, 'audit', 'content/audit/index', 'audit', 6),
(43, '审核处理', 'content:audit:handle', 'button', 42, '', '', '', 1),
(44, '审核查看', 'content:audit:view', 'button', 42, '', '', '', 2),
(45, '审核通过', 'content:audit:approve', 'button', 42, '', '', '', 3),
(46, '审核拒绝', 'content:audit:reject', 'button', 42, '', '', '', 4),

-- 评论管理子菜单（预留）
(47, '评论管理', 'content:comment:manage', 'menu', 17, 'comment', 'content/comment/index', 'message', 7),
(48, '评论创建', 'content:comment:create', 'button', 47, '', '', '', 1),
(49, '评论编辑', 'content:comment:edit', 'button', 47, '', '', '', 2),
(50, '评论删除', 'content:comment:delete', 'button', 47, '', '', '', 3),
(51, '评论查看', 'content:comment:view', 'button', 47, '', '', '', 4);

-- 初始化角色-权限关联关系
-- 超级管理员(ROLE_ROOT)拥有所有权限
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT (SELECT id FROM `role` WHERE code = 'ROLE_ROOT'), id FROM `permission`;

-- 管理员(ROLE_ADMIN)拥有大部分管理权限 (排除高危删除权限)
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT (SELECT id FROM `role` WHERE code = 'ROLE_ADMIN'), id FROM `permission`
WHERE code NOT IN (
    'system:permission:delete',
    'system:role:delete',
    'system:user:delete',
    'system:content:delete',
    'content:category:delete',
    'content:tag:delete',
    'content:comment:delete'
);

-- 协会成员(ROLE_MEMBER)拥有内容相关的基础权限
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
    'system:content:publish',
    'system:content:edit',
    'system:content:query',
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
    'content:comment:view'
);

-- 普通用户(ROLE_USER)拥有基础的内容查看和交互权限
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT (SELECT id FROM `role` WHERE code = 'ROLE_USER'), id FROM `permission`
WHERE code IN (
    -- 内容查看权限
    'system:content:query',
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
    'content:comment:view'
);

