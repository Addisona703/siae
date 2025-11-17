package com.hngy.siae.auth.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 绑定账号信息响应VO
 * 
 * @author SIAE
 */
@Data
public class BoundAccountVO {
    
    /**
     * 提供商: github/google/wechat/qq
     */
    private String provider;
    
    /**
     * 提供商用户名
     */
    private String providerUsername;
    
    /**
     * 提供商邮箱
     */
    private String providerEmail;
    
    /**
     * 提供商头像URL
     */
    private String providerAvatar;
    
    /**
     * 绑定时间
     */
    private LocalDateTime boundAt;
}
