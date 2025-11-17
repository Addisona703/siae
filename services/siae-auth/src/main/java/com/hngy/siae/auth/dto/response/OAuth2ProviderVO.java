package com.hngy.siae.auth.dto.response;

import lombok.Data;

/**
 * OAuth2登录提供商响应VO
 * 
 * @author SIAE
 */
@Data
public class OAuth2ProviderVO {
    
    /**
     * 提供商代码: github/google/wechat/qq
     */
    private String provider;
    
    /**
     * 提供商名称
     */
    private String providerName;
    
    /**
     * 授权URL
     */
    private String authorizationUrl;
    
    /**
     * 图标URL
     */
    private String iconUrl;
}
