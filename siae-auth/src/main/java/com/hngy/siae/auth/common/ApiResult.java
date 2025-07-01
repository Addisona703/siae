package com.hngy.siae.auth.common;

import lombok.Data;

/**
 * API响应结果封装类
 * 
 * @author KEYKB
 */
@Data
public class ApiResult<T> {
    
    /**
     * 状态码
     */
    private int code;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 数据
     */
    private T data;
    
    /**
     * 成功
     *
     * @param data 数据
     * @return 成功结果
     */
    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }
    
    /**
     * 成功
     *
     * @return 成功结果
     */
    public static <T> ApiResult<T> success() {
        return success(null);
    }
    
    /**
     * 失败
     *
     * @param code    状态码
     * @param message 消息
     * @return 失败结果
     */
    public static <T> ApiResult<T> error(int code, String message) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
} 