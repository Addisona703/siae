package com.hngy.siae.common.exception;

import com.hngy.siae.common.result.CommonResultCodeEnum;
import com.hngy.siae.common.result.IResultCode;
import lombok.Getter;

/**
 * 自定义业务异常，用于表示服务层业务错误
 */
@Getter
public class ServiceException extends RuntimeException {

    private final int code;

    public ServiceException(int code, String message) {
        super(message);
        this.code = code;
    }

    public ServiceException(String message) {
        super(message);
        this.code = 500; // 默认业务异常码
    }

    public ServiceException(IResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }
}
