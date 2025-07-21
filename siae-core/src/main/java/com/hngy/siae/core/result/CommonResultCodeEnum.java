package com.hngy.siae.core.result;

import lombok.Getter;

@Getter
public enum CommonResultCodeEnum implements IResultCode {
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    VALIDATE_FAILED(400, "参数校验失败"),
    UNAUTHORIZED(401, "未授权或登录过期"),
    FORBIDDEN(403, "无访问权限"),
    NOT_FOUND(404, "资源未找到"),
    // 自定义更多状态码

    ;

    private final int code;
    private final String message;

    CommonResultCodeEnum(int code, String message) {
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
