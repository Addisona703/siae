package com.hngy.siae.security.permissions;

/**
 * 系统角色常量
 * 对应 auth_db.role 表中的角色代码
 *
 * @author Siae Studio
 */
public class RoleConstants {

    /**
     * 超级管理员角色
     * 拥有所有权限
     */
    public static final String ROLE_ROOT = "ROLE_ROOT";

    /**
     * 管理员角色
     * 拥有大部分管理权限（排除高危删除权限）
     */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    /**
     * 协会成员角色
     * 拥有内容相关的基础权限和用户基础权限
     */
    public static final String ROLE_MEMBER = "ROLE_MEMBER";

    /**
     * 普通用户角色
     * 拥有基础的内容查看和交互权限
     */
    public static final String ROLE_USER = "ROLE_USER";

    // ==================== SpEL 表达式常量 ====================

    /**
     * 任意已认证用户（登录即可访问）
     */
    public static final String ANY_AUTHENTICATED = "isAuthenticated()";

    /**
     * 成员级别及以上（MEMBER, ADMIN, ROOT）
     */
    public static final String MEMBER_LEVEL = "hasAnyRole('MEMBER', 'ADMIN', 'ROOT')";

    /**
     * 管理员级别及以上（ADMIN, ROOT）
     */
    public static final String ADMIN_LEVEL = "hasAnyRole('ADMIN', 'ROOT')";

    /**
     * 仅超级管理员
     */
    public static final String ROOT_ONLY = "hasRole('ROOT')";

    private RoleConstants() {
        // 工具类，禁止实例化
    }
}
