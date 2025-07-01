package com.hngy.siae.common.result;

import lombok.Getter;

/**
 * 用户模块结果码枚举
 *
 * @author KEYKB
 */
@Getter
public enum UserResultCodeEnum implements IResultCode {
    // 用户相关错误码 (1001-1099)
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    USERNAME_ALREADY_EXISTS(1003, "用户名已被使用"),
    EMAIL_ALREADY_EXISTS(1004, "邮箱已被使用"),
    PHONE_ALREADY_EXISTS(1005, "手机号已被使用"),
    PASSWORD_ERROR(1006, "密码错误"),
    ACCOUNT_LOCKED(1007, "账号已被锁定"),
    ACCOUNT_DISABLED(1008, "账号已被禁用"),
    ACCOUNT_EXPIRED(1009, "账号已过期"),
    VERIFICATION_CODE_ERROR(1010, "验证码错误"),
    VERIFICATION_CODE_EXPIRED(1011, "验证码已过期"),
    
    // 用户资料相关错误码 (1100-1199)
    USER_PROFILE_NOT_FOUND(1100, "用户资料不存在"),
    USER_PROFILE_UPDATE_FAILED(1101, "用户资料更新失败"),
    USER_PROFILE_ALREADY_EXISTS(1102, "用户资料已存在"),
    
    // 班级相关错误码 (1200-1299)
    CLASS_NOT_FOUND(1200, "班级不存在"),
    CLASS_ALREADY_EXISTS(1201, "班级已存在"),
    CLASS_USER_RELATION_NOT_FOUND(1202, "用户班级关系不存在"),

    // 会员相关错误码 (1300-1399)
    MEMBER_NOT_FOUND(1300, "会员不存在"),
    MEMBER_ALREADY_EXISTS(1301, "会员已存在"),
    MEMBER_STATUS_ERROR(1302, "会员状态异常"),
    MEMBER_CANDIDATE_NOT_FOUND(1303, "会员候选人不存在"),
    MEMBER_CANDIDATE_ALREADY_PROCESSED(1304, "会员候选申请已处理"),
    MEMBER_CANDIDATE_ALREADY_EXISTS(1305, "该候选成员已存在"),
    MEMBER_CANDIDATE_INACTIVE(1306, "该同学未通过上一次考核"),
    
    // 奖项相关错误码 (1400-1499)
    AWARD_NOT_FOUND(1400, "获奖记录不存在"),
    AWARD_TYPE_NOT_FOUND(1401, "奖项类型不存在"),
    AWARD_TYPE_ALREADY_EXISTS(1402, "奖项类型已存在"),
    AWARD_LEVEL_NOT_FOUND(1403, "奖项等级不存在"),
    AWARD_LEVEL_ALREADY_EXISTS(1404, "奖项等级已存在"),

    // 第三方认证相关错误码 (1500-1599)
    THIRD_PARTY_AUTH_FAILED(1500, "第三方认证失败"),
    THIRD_PARTY_AUTH_EXPIRED(1501, "第三方认证已过期"),
    THIRD_PARTY_AUTH_ALREADY_BOUND(1502, "第三方账号已被绑定"),
    THIRD_PARTY_AUTH_NOT_FOUND(1503, "第三方认证信息不存在"),

    // 学院和专业相关错误码 (1600-1699)
    COLLEGE_NOT_FOUND(1600, "该学院还未录入"),
    MAJOR_NOT_FOUND(1601, "协会暂不对该专业开放"),

    // 其他错误码 (1900-1999)
    OPERATION_NOT_ALLOWED(1900, "操作不允许"),
    INSUFFICIENT_PERMISSIONS(1901, "权限不足"),
    DATA_INTEGRITY_VIOLATION(1902, "数据完整性约束异常"),
    ;

    private final int code;
    private final String message;

    UserResultCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
} 