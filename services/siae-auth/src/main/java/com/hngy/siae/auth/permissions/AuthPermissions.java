package com.hngy.siae.auth.permissions;

/**
 * 认证服务权限常量
 * 定义认证服务相关的权限标识符
 *
 * @author Siae Studio
 */
public final class AuthPermissions {

    private AuthPermissions() {
        // 工具类，禁止实例化
    }

    // ==================== 角色管理权限 ====================
    
    /**
     * 角色查询权限
     */
    public static final String AUTH_ROLE_QUERY = "auth:role:query";
    
    /**
     * 角色新增权限
     */
    public static final String AUTH_ROLE_ADD = "auth:role:add";
    
    /**
     * 角色编辑权限
     */
    public static final String AUTH_ROLE_EDIT = "auth:role:edit";
    
    /**
     * 角色删除权限
     */
    public static final String AUTH_ROLE_DELETE = "auth:role:delete";

    // ==================== 权限管理权限 ====================
    
    /**
     * 权限查询权限
     */
    public static final String AUTH_PERMISSION_QUERY = "auth:permission:query";
    
    /**
     * 权限新增权限
     */
    public static final String AUTH_PERMISSION_ADD = "auth:permission:add";
    
    /**
     * 权限编辑权限
     */
    public static final String AUTH_PERMISSION_EDIT = "auth:permission:edit";
    
    /**
     * 权限删除权限
     */
    public static final String AUTH_PERMISSION_DELETE = "auth:permission:delete";

    // ==================== 用户角色管理权限 ====================
    
    /**
     * 用户角色查询权限
     */
    public static final String AUTH_USER_ROLE_QUERY = "auth:user-role:query";
    
    /**
     * 用户角色分配权限
     */
    public static final String AUTH_USER_ROLE_ASSIGN = "auth:user-role:assign";
    
    /**
     * 用户角色更新权限
     */
    public static final String AUTH_USER_ROLE_UPDATE = "auth:user-role:update";

    // ==================== 用户权限管理权限 ====================
    
    /**
     * 用户权限查询权限
     */
    public static final String AUTH_USER_PERMISSION_QUERY = "auth:user-permission:query";
    
    /**
     * 用户权限分配权限
     */
    public static final String AUTH_USER_PERMISSION_ASSIGN = "auth:user-permission:assign";
    
    /**
     * 用户权限移除权限
     */
    public static final String AUTH_USER_PERMISSION_REMOVE = "auth:user-permission:remove";

    // ==================== 日志管理权限 ====================
    
    /**
     * 日志查询权限
     */
    public static final String AUTH_LOG_QUERY = "auth:log:query";
}
