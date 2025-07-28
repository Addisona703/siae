package com.hngy.siae.core.permissions;

/**
 * 认证模块权限常量定义
 *
 * 命名规范：模块:资源:操作（如 auth:user:query）
 * 常量命名规范：AUTH_资源_操作（如 AUTH_USER_QUERY）
 *
 * @author KEYKB
 * @date 2025/01/01
 */
public class AuthPermissions {
    // ==================== 用户管理权限 ====================
    /** 查询用户 */
    public static final String AUTH_USER_QUERY = "auth:user:query";

    /** 新增用户 */
    public static final String AUTH_USER_ADD = "auth:user:add";

    /** 修改用户 */
    public static final String AUTH_USER_EDIT = "auth:user:edit";

    /** 删除用户 */
    public static final String AUTH_USER_DELETE = "auth:user:delete";

    // ==================== 角色管理权限 ====================
    /** 查询角色 */
    public static final String AUTH_ROLE_QUERY = "auth:role:query";

    /** 新增角色 */
    public static final String AUTH_ROLE_ADD = "auth:role:add";

    /** 修改角色 */
    public static final String AUTH_ROLE_EDIT = "auth:role:edit";

    /** 删除角色 */
    public static final String AUTH_ROLE_DELETE = "auth:role:delete";

    // ==================== 权限管理权限 ====================
    /** 查询权限 */
    public static final String AUTH_PERMISSION_QUERY = "auth:permission:query";

    /** 新增权限 */
    public static final String AUTH_PERMISSION_ADD = "auth:permission:add";

    /** 修改权限 */
    public static final String AUTH_PERMISSION_EDIT = "auth:permission:edit";

    /** 删除权限 */
    public static final String AUTH_PERMISSION_DELETE = "auth:permission:delete";

    // ==================== 日志管理权限 ====================
    /** 查询登录日志 */
    public static final String AUTH_LOG_QUERY = "auth:log:query";

    /** 导出登录日志 */
    public static final String AUTH_LOG_EXPORT = "auth:log:export";

    // ==================== 用户角色关联权限 ====================
    /** 分配用户角色 */
    public static final String AUTH_USER_ROLE_ASSIGN = "auth:user:role:assign";

    /** 查询用户角色 */
    public static final String AUTH_USER_ROLE_QUERY = "auth:user:role:query";

    /** 更新用户角色 */
    public static final String AUTH_USER_ROLE_UPDATE = "auth:user:role:update";

    /** 移除用户角色 */
    public static final String AUTH_USER_ROLE_REMOVE = "auth:user:role:remove";

    // ==================== 用户权限关联权限 ====================
    /** 分配用户权限 */
    public static final String AUTH_USER_PERMISSION_ASSIGN = "auth:user:permission:assign";

    /** 查询用户权限 */
    public static final String AUTH_USER_PERMISSION_QUERY = "auth:user:permission:query";

    /** 移除用户权限 */
    public static final String AUTH_USER_PERMISSION_REMOVE = "auth:user:permission:remove";
}
