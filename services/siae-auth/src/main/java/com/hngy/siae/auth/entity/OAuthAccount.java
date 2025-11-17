package com.hngy.siae.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 第三方账号绑定实体类
 * 
 * @author SIAE
 */
@Data
@TableName("oauth_account")
public class OAuthAccount {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 提供商: github/google/wechat/qq
     */
    private String provider;
    
    /**
     * 提供商用户ID
     */
    private String providerUserId;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 访问令牌
     */
    private String accessToken;
    
    /**
     * 刷新令牌
     */
    private String refreshToken;
    
    /**
     * 令牌过期时间
     */
    private LocalDateTime expiresAt;
    
    /**
     * 原始用户信息(JSON)
     */
    private String rawJson;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
