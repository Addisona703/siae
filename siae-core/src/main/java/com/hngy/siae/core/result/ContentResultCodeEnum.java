package com.hngy.siae.core.result;

import lombok.Getter;

@Getter
public class ContentResultCodeEnum implements IResultCode {
    private final int code;
    private final String message;

    public ContentResultCodeEnum(int code, String message) {
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
