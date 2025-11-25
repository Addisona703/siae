package com.hngy.siae.core.permissions;

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

    // ==================== 组合表达式（超管默认放行，无需写入表达式） ====================

    /**
     * 管理员级别
     * 注：超管默认放行，无需在表达式中包含 ROLE_ROOT
     */
    public static final String ADMIN_LEVEL = "hasRole('" + ROLE_ADMIN + "')";

    /**
     * 成员级别（管理员 或 协会成员）
     * 注：超管默认放行，无需在表达式中包含 ROLE_ROOT
     */
    public static final String MEMBER_LEVEL = "hasRole('" + ROLE_ADMIN + "') or hasRole('" + ROLE_MEMBER + "')";

    /**
     * 所有已认证用户（任何角色）
     * 注：超管默认放行，无需在表达式中包含 ROLE_ROOT
     */
    public static final String ANY_AUTHENTICATED = "hasRole('" + ROLE_ADMIN + "') or hasRole('" + ROLE_MEMBER + "') or hasRole('" + ROLE_USER + "')";

    private RoleConstants() {
        // 工具类，禁止实例化
    }
}
