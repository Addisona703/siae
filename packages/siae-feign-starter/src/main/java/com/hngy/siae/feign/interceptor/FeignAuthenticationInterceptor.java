package com.hngy.siae.feign.interceptor;

import com.hngy.siae.core.config.AuthProperties;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Feign服务间调用认证拦截器
 * 职责：自动为服务间调用添加认证头
 *
 * @author SIAE开发团队
 */
@Slf4j
@RequiredArgsConstructor
public class FeignAuthenticationInterceptor implements RequestInterceptor {

    private final AuthProperties authProperties;
    private final String currentServiceName;

    @Override
    public void apply(RequestTemplate template) {
        try {
            log.info("[Feign拦截器] 开始添加认证头, 服务: {}, 目标URL: {}", currentServiceName, template.url());
            
            // 1. 添加内部服务调用标识
            String secretKey = authProperties.getInternalSecretKey();
            log.info("[Feign拦截器] internalSecretKey: {}", secretKey != null ? secretKey.substring(0, Math.min(8, secretKey.length())) + "..." : "NULL");
            
            template.header("X-Internal-Service-Call", secretKey);
            template.header("X-Caller-Service", currentServiceName);
            template.header("X-Call-Timestamp", String.valueOf(System.currentTimeMillis()));
            
            // 2. 添加 User-Agent 标识，用于 Security 过滤器识别服务间调用
            template.header("User-Agent", "Feign/" + currentServiceName);

            // 3. 如果当前有用户上下文，传递用户ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object details = authentication.getDetails();
                if (details instanceof Long) {
                    template.header("X-On-Behalf-Of-User", details.toString());
                    log.info("[Feign拦截器] 添加用户上下文: user={}", details);
                }
            }

            log.info("[Feign拦截器] 认证头添加完成: {} -> {}", currentServiceName, template.url());

        } catch (Exception e) {
            log.error("Failed to add authentication headers for Feign request", e);
        }
    }
}
