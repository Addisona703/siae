package com.hngy.siae.auth.dto.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户注册响应视图对象
 * 
 * @author KEYKB
 */
@Data
public class RegisterVO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
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
     * 过期时间（秒）
     */
    private Long expiresIn;
} 