package com.hngy.siae.core.result;

import lombok.Getter;

@Getter
public enum AuthResultCodeEnum implements IResultCode {
    // 认证相关错误码 (2001-2099)
    LOGIN_FAILED(2001, "登录失败"),
    USER_NOT_FOUND(2002, "用户不存在"),
    PASSWORD_ERROR(2003, "密码错误"),
    ACCOUNT_DISABLED(2004, "用户已禁用"),
    ACCOUNT_LOCKED(2005, "账号已被锁定"),
    TOKEN_INVALID(2006, "令牌无效"),
    TOKEN_EXPIRED(2007, "令牌已过期"),
    REFRESH_TOKEN_INVALID(2008, "刷新令牌无效"),
    LOGOUT_FAILED(2009, "登出失败"),

    // 注册相关错误码 (2100-2199)
    REGISTER_FAILED(2100, "注册失败"),
    USERNAME_ALREADY_EXISTS(2101, "用户名已被使用"),
    EMAIL_ALREADY_EXISTS(2102, "邮箱已被使用"),
    PHONE_ALREADY_EXISTS(2103, "手机号已被使用"),
    VERIFICATION_CODE_ERROR(2104, "验证码错误"),
    VERIFICATION_CODE_EXPIRED(2105, "验证码已过期"),
    PASSWORD_MISMATCH(2106, "两次输入的密码不一致"),
    USER_CREATION_FAILED(2107, "用户创建失败"),

    // 权限相关错误码 (2200-2299)
    PERMISSION_DENIED(2200, "权限不足"),
    ROLE_NOT_FOUND(2201, "角色不存在"),
    PERMISSION_NOT_FOUND(2202, "权限不存在"),
    PERMISSION_CODE_EXISTS(2203, "权限已存在"),
    ROLE_CODE_EXISTS(2204, "角色编码已存在"),
    SYSTEM_ROLE_CANNOT_DELETE(2205, "系统内置角色不可删除"),
    SYSTEM_ROLE_CANNOT_UPDATE(2206, "系统内置角色不可修改"),
    ROLE_HAS_USERS(2207, "角色已分配给用户，无法删除"),
    ROLE_CREATE_FAILED(2208, "角色创建失败"),
    PERMISSION_NOT_EXISTS(2209, "权限不存在"),
    ROLE_NOT_EXISTS(2210, "角色不存在");

    private final int code;
    private final String message;

    AuthResultCodeEnum(int code, String message) {
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
