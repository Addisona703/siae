package com.hngy.siae.user.permissions;

/**
 * 用户服务权限常量
 * 定义用户服务相关的权限标识符
 *
 * @author Siae Studio
 */
public final class UserPermissions {

    private UserPermissions() {
        // 工具类，禁止实例化
    }

    // ==================== 用户管理权限 ====================
    
    /**
     * 用户创建权限
     */
    public static final String USER_CREATE = "user:user:create";
    
    /**
     * 用户更新权限
     */
    public static final String USER_UPDATE = "user:user:update";
    
    /**
     * 用户删除权限
     */
    public static final String USER_DELETE = "user:user:delete";

    // ==================== 部门管理权限 ====================
    
    /**
     * 部门创建权限
     */
    public static final String USER_DEPARTMENT_CREATE = "user:department:create";
    
    /**
     * 部门更新权限
     */
    public static final String USER_DEPARTMENT_UPDATE = "user:department:update";
    
    /**
     * 部门删除权限
     */
    public static final String USER_DEPARTMENT_DELETE = "user:department:delete";

    // ==================== 职位管理权限 ====================
    
    /**
     * 职位创建权限
     */
    public static final String USER_POSITION_CREATE = "user:position:create";
    
    /**
     * 职位更新权限
     */
    public static final String USER_POSITION_UPDATE = "user:position:update";
    
    /**
     * 职位删除权限
     */
    public static final String USER_POSITION_DELETE = "user:position:delete";

    // ==================== 专业管理权限 ====================
    
    /**
     * 专业创建权限
     */
    public static final String USER_MAJOR_CREATE = "user:major:create";
    
    /**
     * 专业更新权限
     */
    public static final String USER_MAJOR_UPDATE = "user:major:update";
    
    /**
     * 专业删除权限
     */
    public static final String USER_MAJOR_DELETE = "user:major:delete";

    // ==================== 奖项类型管理权限 ====================
    
    /**
     * 奖项类型创建权限
     */
    public static final String USER_AWARD_TYPE_CREATE = "user:award-type:create";
    
    /**
     * 奖项类型更新权限
     */
    public static final String USER_AWARD_TYPE_UPDATE = "user:award-type:update";
    
    /**
     * 奖项类型删除权限
     */
    public static final String USER_AWARD_TYPE_DELETE = "user:award-type:delete";

    // ==================== 奖项等级管理权限 ====================
    
    /**
     * 奖项等级创建权限
     */
    public static final String USER_AWARD_LEVEL_CREATE = "user:award-level:create";
    
    /**
     * 奖项等级更新权限
     */
    public static final String USER_AWARD_LEVEL_UPDATE = "user:award-level:update";
    
    /**
     * 奖项等级删除权限
     */
    public static final String USER_AWARD_LEVEL_DELETE = "user:award-level:delete";

    // ==================== 用户获奖记录管理权限 ====================
    
    /**
     * 用户获奖记录创建权限
     */
    public static final String USER_AWARD_CREATE = "user:award:create";
    
    /**
     * 用户获奖记录更新权限
     */
    public static final String USER_AWARD_UPDATE = "user:award:update";
    
    /**
     * 用户获奖记录查看权限
     */
    public static final String USER_AWARD_VIEW = "user:award:view";
    
    /**
     * 用户获奖记录列表权限
     */
    public static final String USER_AWARD_LIST = "user:award:list";
    
    /**
     * 用户获奖记录删除权限
     */
    public static final String USER_AWARD_DELETE = "user:award:delete";
}
