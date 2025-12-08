package com.hngy.siae.notification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 通知服务配置属性
 *
 * @author KEYKB
 */
@Data
@Component
@ConfigurationProperties(prefix = "siae.notification")
public class NotificationProperties {

    /**
     * 前端基础URL，用于拼接通知中的跳转链接
     * 例如: http://localhost:5173
     */
    private String frontendBaseUrl = "http://localhost:5173";

    /**
     * 构建完整的前端URL
     * 
     * @param path 相对路径，如 /content/11
     * @return 完整URL，如 http://localhost:5173/content/11
     */
    public String buildFullUrl(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        
        String baseUrl = frontendBaseUrl.endsWith("/") 
            ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1) 
            : frontendBaseUrl;
        
        String relativePath = path.startsWith("/") ? path : "/" + path;
        
        return baseUrl + relativePath;
    }
}
