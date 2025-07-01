package com.hngy.siae.auth.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录失败日志响应DTO
 * 
 * @author KEYKB
 */
@Data
public class LoginFailResponse {
    
    /**
     * 日志ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 登录IP
     */
    private String loginIp;
    
    /**
     * 失败原因
     */
    private String failReason;
    
    /**
     * 失败时间
     */
    private LocalDateTime failTime;
} 