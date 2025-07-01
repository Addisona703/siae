package com.hngy.siae.auth.dto;

import lombok.Data;

/**
 * 登录响应DTO
 * 
 * @author KEYKB
 */
@Data
public class LoginResponse {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 访问令牌
     */
    private String accessToken;
    
    /**
     * 刷新令牌
     */
    private String refreshToken;
    
    /**
     * 令牌类型
     */
    private String tokenType;
    
    /**
     * 过期时间(秒)
     */
    private Long expiresIn;
} 