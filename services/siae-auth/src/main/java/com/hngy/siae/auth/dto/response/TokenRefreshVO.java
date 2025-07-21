package com.hngy.siae.auth.dto.response;

import lombok.Data;

/**
 * 刷新令牌响应DTO
 * 
 * @author KEYKB
 */
@Data
public class TokenRefreshVO {
    
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