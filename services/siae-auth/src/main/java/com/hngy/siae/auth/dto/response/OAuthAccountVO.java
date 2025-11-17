package com.hngy.siae.auth.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 第三方账号绑定信息VO
 * 
 * @author SIAE
 */
@Data
public class OAuthAccountVO {
    
    /**
     * 提供商: qq/wx/github
     */
    private String provider;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
