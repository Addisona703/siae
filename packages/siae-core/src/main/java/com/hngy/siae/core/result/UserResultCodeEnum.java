package com.hngy.siae.core.result;

import lombok.Getter;

/**
 * 用户模块结果码枚举
 * <p>
 * 包含用户、奖项、专业、部门、职位等相关的错误码定义
 *
 * @author KEYKB
 */
@Getter
public enum UserResultCodeEnum implements IResultCode {

    // ==================== 用户相关错误码 (1001-1099) ====================
    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_ALREADY_EXISTS(1003, "用户名已被使用"),
    STUDENT_ID_ALREADY_EXISTS(1007, "学号已存在"),

    // ==================== 奖项相关错误码 (1400-1499) ====================
    AWARD_NOT_FOUND(1400, "获奖记录不存在"),
    AWARD_TYPE_NOT_FOUND(1401, "奖项类型不存在"),
    AWARD_TYPE_ALREADY_EXISTS(1402, "奖项类型已存在"),
    AWARD_LEVEL_NOT_FOUND(1403, "奖项等级不存在"),
    AWARD_LEVEL_ALREADY_EXISTS(1404, "奖项等级已存在"),

    // ==================== 专业相关错误码 (1600-1699) ====================
    MAJOR_NOT_FOUND(1601, "专业不存在"),
    MAJOR_ALREADY_EXISTS(1602, "专业已存在"),
    MAJOR_CODE_ALREADY_EXISTS(1603, "专业编码已存在"),

    // ==================== 部门相关错误码 (1700-1799) ====================
    DEPARTMENT_NOT_FOUND(1700, "部门不存在"),
    DEPARTMENT_ALREADY_EXISTS(1701, "部门已存在"),

    // ==================== 职位相关错误码 (1800-1899) ====================
    POSITION_NOT_FOUND(1800, "职位不存在"),
    POSITION_ALREADY_EXISTS(1801, "职位已存在"),

    // ==================== 成员相关错误码 (1900-1999) ====================
    MEMBERSHIP_NOT_FOUND(1900, "成员不存在"),
    MEMBERSHIP_ALREADY_EXISTS(1901, "用户已经是成员，无法重复申请"),
    MEMBERSHIP_NOT_CANDIDATE(1902, "该成员不是候选成员，无法转正"),
    MEMBERSHIP_STATUS_INVALID(1903, "成员状态不正确，无法执行此操作"),
    MEMBERSHIP_EXPELLED(1904, "该用户已被开除，无法再次申请加入"),

    // ==================== 简历相关错误码 (2000-2099) ====================
    RESUME_NOT_FOUND(2000, "简历不存在"),
    RESUME_ALREADY_EXISTS(2001, "简历已存在"),
    ;

    private final int code;
    private final String message;

    UserResultCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
} 