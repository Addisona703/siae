package com.hngy.siae.media.config;

import com.hngy.siae.media.security.TenantInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * <p>
 * 配置内容：
 * - 租户拦截器：提取租户和用户信息到上下文
 * <p>
 * 执行顺序：
 * 1. ServiceAuthenticationFilter（security-starter）- 认证和权限
 * 2. TenantInterceptor（此处配置）- 租户上下文管理
 * 3. Controller 方法执行
 *
 * @author SIAE Team
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/actuator/**",      // 健康检查
                    "/swagger-ui/**",    // API文档
                    "/v3/api-docs/**"    // OpenAPI规范
                );
    }

}
