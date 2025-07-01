package com.hngy.siae.auth.common;

import lombok.Getter;

/**
 * 业务异常类
 * 
 * @author KEYKB
 */
@Getter
public class BusinessException extends RuntimeException {
    
    /**
     * 错误码
     */
    private final int code;
    
    /**
     * 构造函数
     *
     * @param code    错误码
     * @param message 错误信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    /**
     * 构造函数
     *
     * @param message 错误信息
     */
    public BusinessException(String message) {
        this(400, message);
    }
} 