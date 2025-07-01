package com.hngy.siae.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 返回结果码枚举类
 */
@AllArgsConstructor
@Getter
public enum ResultCodeEnum{

    // 通用成功与失败
    SUCCESS(200, "操作成功"),
    FAILURE(500, "操作失败"),

    // 认证与授权相关
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "权限不足，无法访问"),
    NOT_FOUND(404, "请求的资源不存在"),

    // 参数校验相关
    BAD_REQUEST(400, "请求参数错误"),
    INVALID_PARAMETER(2002, "参数校验失败"),

    // 业务逻辑相关
    DATA_NOT_FOUND(2001, "查询数据不存在"),
    RESOURCE_ALREADY_EXISTS(2004, "资源已存在"),
    OPERATION_FORBIDDEN(2003, "操作被禁止");

    private final int code;
    private final String message;
}
