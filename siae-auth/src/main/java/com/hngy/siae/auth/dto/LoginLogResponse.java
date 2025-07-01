package com.hngy.siae.auth.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志响应DTO
 * 
 * @author KEYKB
 */
@Data
public class LoginLogResponse {
    
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
     * 登录地点
     */
    private String loginLocation;
    
    /**
     * 浏览器类型
     */
    private String browser;
    
    /**
     * 操作系统
     */
    private String os;
    
    /**
     * 登录状态（0失败 1成功）
     */
    private Integer status;
    
    /**
     * 提示消息
     */
    private String msg;
    
    /**
     * 登录时间
     */
    private LocalDateTime loginTime;
} 