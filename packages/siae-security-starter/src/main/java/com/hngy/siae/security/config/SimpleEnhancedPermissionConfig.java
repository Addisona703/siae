package com.hngy.siae.security.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * 简化的增强权限控制配置
 * 
 * 通过最小化的配置启用增强权限控制功能：
 * 1. 确保方法级安全注解生效
 * 2. 支持SpEL表达式中调用AuthUtil的方法
 * 
 * @author SIAE开发团队
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "siae.security", name = "enabled", havingValue = "true", matchIfMissing = false)
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SimpleEnhancedPermissionConfig {

    public SimpleEnhancedPermissionConfig() {
        log.info("SIAE简化增强权限控制已启用");
        log.info("支持功能：");
        log.info("  - 全局超级管理员放行机制");
        log.info("  - 增强的SpEL表达式支持");
        log.info("  - 与现有权限服务集成");
        log.info("使用方法：");
        log.info("  @PreAuthorize(\"@authUtil.isSuperAdmin() or hasRole('ADMIN')\")");
        log.info("  @PreAuthorize(\"@authUtil.hasPermissionOrSuperAdmin('USER_VIEW')\")");
        log.info("  @PreAuthorize(\"@authUtil.hasRoleOrSuperAdmin('ADMIN')\")");
    }
}
