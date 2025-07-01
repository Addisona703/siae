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
-- 资源管理
(17, '资源管理', 'content:manage', 'menu', NULL, '/resource', 'Layout', 'documentation', 2),
-- 资源列表
(18, '资源列表', 'content:resource:list', 'menu', 17, 'list', 'resource/list/index', 'list', 1),
(19, '资源查询', 'content:resource:query', 'button', 18, '', '', '', 1),
(20, '资源新增', 'content:resource:add', 'button', 18, '', '', '', 2),
(21, '资源修改', 'content:resource:edit', 'button', 18, '', '', '', 3),
(22, '资源删除', 'content:resource:delete', 'button', 18, '', '', '', 4),
-- 分类管理
(23, '分类管理', 'content:category:manage', 'menu', 17, 'category', 'resource/category/index', 'tree', 2),
(24, '分类查询', 'content:category:query', 'button', 23, '', '', '', 1),
(25, '分类新增', 'content:category:add', 'button', 23, '', '', '', 2),
(26, '分类修改', 'content:category:edit', 'button', 23, '', '', '', 3),
(27, '分类删除', 'content:category:delete', 'button', 23, '', '', '', 4),
-- 标签管理
(28, '标签管理', 'content:tag:manage', 'menu', 17, 'tag', 'resource/tag/index', 'tag', 3),
(29, '标签查询', 'content:tag:query', 'button', 28, '', '', '', 1),
(30, '标签新增', 'content:tag:add', 'button', 28, '', '', '', 2),
(31, '标签修改', 'content:tag:edit', 'button', 28, '', '', '', 3),
(32, '标签删除', 'content:tag:delete', 'button', 28, '', '', '', 4),
-- 审核管理
(33, '审核管理', 'content:audit:manage', 'menu', 17, 'audit', 'resource/audit/index', 'audit', 4),
(34, '审核查询', 'content:audit:query', 'button', 33, '', '', '', 1),
(35, '审核操作', 'content:audit:operate', 'button', 33, '', '', '', 2),
(36, '审核记录', 'content:audit:record', 'button', 33, '', '', '', 3);

-- 初始化角色-权限关联关系
-- 超级管理员(ROLE_ROOT)拥有所有权限
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT (SELECT id FROM `role` WHERE code = 'ROLE_ROOT'), id FROM `permission`;

-- 管理员(ROLE_ADMIN)拥有大部分管理权限 (排除高危删除权限)
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT (SELECT id FROM `role` WHERE code = 'ROLE_ADMIN'), id FROM `permission`
WHERE code NOT IN ('system:permission:delete', 'system:role:delete', 'system:user:delete', 'content:resource:delete', 'content:category:delete', 'content:tag:delete');

-- 协会成员(ROLE_MEMBER)拥有资源查看、创建、修改和审核查询的权限
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT (SELECT id FROM `role` WHERE code = 'ROLE_MEMBER'), id FROM `permission`
WHERE code LIKE 'content:resource:%' OR code LIKE 'content:category:query' OR code LIKE 'content:tag:query' OR code LIKE 'content:audit:query';

