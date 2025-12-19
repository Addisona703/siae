package com.hngy.siae.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * OAuth第三方登录配置属性
 * 
 * @author KEYKB
 */
@Data
@Component
@ConfigurationProperties(prefix = "siae.oauth")
public class OAuthProperties {
    
    private QQConfig qq;
    private GiteeConfig gitee;
    private GithubConfig github;
    private StateConfig state;
    
    /**
     * OAuth回调成功后重定向的前端地址
     * 例如: http://localhost 或 http://localhost:7090
     */
    private String frontendBaseUrl = "http://localhost";
    
    /**
     * QQ登录配置
     */
    @Data
    public static class QQConfig {
        private String appId;
        private String appKey;
        private String redirectUri;
    }
    
    /**
     * Gitee登录配置
     */
    @Data
    public static class GiteeConfig {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
    }
    
    /**
     * GitHub登录配置
     */
    @Data
    public static class GithubConfig {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
    }
    
    /**
     * State参数配置
     */
    @Data
    public static class StateConfig {
        /**
         * state参数有效期（秒），默认5分钟
         */
        private int expireSeconds = 300;
    }
}
