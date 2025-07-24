package com.hngy.siae.core.exception;

import com.hngy.siae.core.result.IResultCode;
import lombok.Getter;

/**
 * 通用业务异常类
 * 用于表示业务逻辑错误，区别于系统异常
 * 
 * @author SIAE开发团队
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    /**
     * 构造业务异常
     * 
     * @param code 错误码
     * @param message 错误信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造业务异常（使用默认错误码400）
     * 
     * @param message 错误信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400; // 默认业务异常码
    }

    /**
     * 构造业务异常（使用结果码枚举）
     * 
     * @param resultCode 结果码枚举
     */
    public BusinessException(IResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    /**
     * 构造业务异常（使用结果码枚举和自定义消息）
     * 
     * @param resultCode 结果码枚举
     * @param message 自定义错误信息
     */
    public BusinessException(IResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }
}
