package com.hngy.siae.auth.dto.response;

import lombok.Data;

/**
 * OAuth2用户信息响应VO
 * 
 * @author SIAE
 */
@Data
public class OAuth2UserInfoVO {
    
    /**
     * 用户ID
     */
    private String sub;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 昵称
     */
    private String name;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 头像URL
     */
    private String picture;
    
    /**
     * 邮箱是否验证
     */
    private Boolean emailVerified;
}
