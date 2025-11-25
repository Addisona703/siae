package com.hngy.siae.core.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(CommonResultCodeEnum.SUCCESS.getCode(), CommonResultCodeEnum.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(CommonResultCodeEnum.SUCCESS.getCode(), message, data);
    }

    public static <T> Result<T> error() {
        return error(CommonResultCodeEnum.ERROR.getMessage());
    }

    public static <T> Result<T> error(String message) {
        return error(CommonResultCodeEnum.ERROR.getCode(), message);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
    
    public static <T> Result<T> error(IResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }
    
    public static <T> Result<T> error(IResultCode resultCode, T data) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), data);
    }
}
