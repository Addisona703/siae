package com.hngy.siae.core.permissions;

/**
 * 用户模块权限常量定义
 * <p>
 * 命名规范：模块:资源:操作（如 user:profile:view）
 * 常量命名规范：USER_资源_操作（如 USER_PROFILE_VIEW）
 *
 * @author KEYKB
 * &#064;date  2025/07/21
 */
public class UserPermissions {

    // ==================== 用户管理权限 ====================
    /** 创建用户 */
    public static final String USER_CREATE = "user:create";

    /** 更新用户 */
    public static final String USER_UPDATE = "user:update";

    /** 删除用户 */
    public static final String USER_DELETE = "user:delete";

    /** 查询用户 */
    public static final String USER_VIEW = "user:view";

    /** 分页查询用户列表 */
    public static final String USER_LIST = "user:list";

    // ==================== 用户详情管理权限 ====================
    /** 创建用户详情 */
    public static final String USER_PROFILE_CREATE = "user:profile:create";

    /** 更新用户详情 */
    public static final String USER_PROFILE_UPDATE = "user:profile:update";

    /** 删除用户详情 */
    public static final String USER_PROFILE_DELETE = "user:profile:delete";

    /** 查询用户详情 */
    public static final String USER_PROFILE_VIEW = "user:profile:view";

    // ==================== 正式成员管理权限 ====================
    /** 创建正式成员 - 允许创建新的正式成员记录，包括直接创建和从候选成员转换 */
    public static final String USER_MEMBER_CREATE = "user:member:create";

    /** 更新正式成员 - 允许修改正式成员的基本信息，如部门、职位、状态等 */
    public static final String USER_MEMBER_UPDATE = "user:member:update";

    /** 查询正式成员 - 允许查看正式成员的详细信息 */
    public static final String USER_MEMBER_VIEW = "user:member:view";

    /** 分页查询正式成员列表 - 允许分页查询和条件筛选正式成员列表 */
    public static final String USER_MEMBER_LIST = "user:member:list";

    /** 删除正式成员 - 允许删除正式成员记录（逻辑删除） */
    public static final String USER_MEMBER_DELETE = "user:member:delete";

    // ==================== 候选成员管理权限 ====================
    /** 添加候选成员 */
    public static final String USER_CANDIDATE_CREATE = "user:candidate:create";

    /** 更新候选成员 */
    public static final String USER_CANDIDATE_UPDATE = "user:candidate:update";

    /** 删除候选成员 */
    public static final String USER_CANDIDATE_DELETE = "user:candidate:delete";

    /** 查询候选成员 */
    public static final String USER_CANDIDATE_VIEW = "user:candidate:view";

    /** 分页查询候选成员列表 */
    public static final String USER_CANDIDATE_LIST = "user:candidate:list";

    // ==================== 班级管理权限 ====================
    /** 创建班级 */
    public static final String USER_CLASS_CREATE = "user:class:create";

    /** 更新班级 */
    public static final String USER_CLASS_UPDATE = "user:class:update";

    /** 删除班级 */
    public static final String USER_CLASS_DELETE = "user:class:delete";

    /** 查询班级 */
    public static final String USER_CLASS_VIEW = "user:class:view";

    /** 分页查询班级列表 */
    public static final String USER_CLASS_LIST = "user:class:list";

    // ==================== 奖项类型管理权限 ====================
    /** 创建奖项类型 */
    public static final String USER_AWARD_TYPE_CREATE = "user:award-type:create";

    /** 更新奖项类型 */
    public static final String USER_AWARD_TYPE_UPDATE = "user:award-type:update";

    /** 删除奖项类型 */
    public static final String USER_AWARD_TYPE_DELETE = "user:award-type:delete";

    /** 查询奖项类型 */
    public static final String USER_AWARD_TYPE_VIEW = "user:award-type:view";

    /** 分页查询奖项类型列表 */
    public static final String USER_AWARD_TYPE_LIST = "user:award-type:list";

    // ==================== 奖项等级管理权限 ====================
    /** 创建奖项等级 */
    public static final String USER_AWARD_LEVEL_CREATE = "user:award-level:create";

    /** 更新奖项等级 */
    public static final String USER_AWARD_LEVEL_UPDATE = "user:award-level:update";

    /** 删除奖项等级 */
    public static final String USER_AWARD_LEVEL_DELETE = "user:award-level:delete";

    /** 查询奖项等级 */
    public static final String USER_AWARD_LEVEL_VIEW = "user:award-level:view";

    /** 分页查询奖项等级列表 */
    public static final String USER_AWARD_LEVEL_LIST = "user:award-level:list";

    // ==================== 用户获奖记录管理权限 ====================
    /** 创建用户获奖记录 */
    public static final String USER_AWARD_CREATE = "user:award:create";

    /** 更新用户获奖记录 */
    public static final String USER_AWARD_UPDATE = "user:award:update";

    /** 删除用户获奖记录 */
    public static final String USER_AWARD_DELETE = "user:award:delete";

    /** 查询用户获奖记录 */
    public static final String USER_AWARD_VIEW = "user:award:view";

    /** 分页查询用户获奖记录列表 */
    public static final String USER_AWARD_LIST = "user:award:list";

    // ==================== 专业管理权限 ====================
    /** 创建专业 */
    public static final String USER_MAJOR_CREATE = "user:major:create";

    /** 更新专业 */
    public static final String USER_MAJOR_UPDATE = "user:major:update";

    /** 删除专业 */
    public static final String USER_MAJOR_DELETE = "user:major:delete";

    /** 查询专业 */
    public static final String USER_MAJOR_VIEW = "user:major:view";

    /** 查询专业列表 */
    public static final String USER_MAJOR_LIST = "user:major:list";

    // ==================== 部门管理权限 ====================
    /** 创建部门 */
    public static final String USER_DEPARTMENT_CREATE = "user:department:create";

    /** 更新部门 */
    public static final String USER_DEPARTMENT_UPDATE = "user:department:update";

    /** 删除部门 */
    public static final String USER_DEPARTMENT_DELETE = "user:department:delete";

    /** 查询部门 */
    public static final String USER_DEPARTMENT_VIEW = "user:department:view";

    /** 查询部门列表 */
    public static final String USER_DEPARTMENT_LIST = "user:department:list";

    // ==================== 职位管理权限 ====================
    /** 创建职位 */
    public static final String USER_POSITION_CREATE = "user:position:create";

    /** 更新职位 */
    public static final String USER_POSITION_UPDATE = "user:position:update";

    /** 删除职位 */
    public static final String USER_POSITION_DELETE = "user:position:delete";

    /** 查询职位 */
    public static final String USER_POSITION_VIEW = "user:position:view";

    /** 查询职位列表 */
    public static final String USER_POSITION_LIST = "user:position:list";
}
