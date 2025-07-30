package com.hngy.siae.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 安全相关配置属性
 * 
 * @author SIAE开发团队
 */
@Data
@ConfigurationProperties(prefix = "siae.security")
public class SecurityProperties {

    /**
     * 是否启用安全功能
     */
    private boolean enabled = true;

    /**
     * JWT配置
     */
    private Jwt jwt = new Jwt();

    /**
     * 权限缓存配置
     */
    private Permission permission = new Permission();

    /**
     * 增强权限控制配置
     */
    private EnhancedPermission enhancedPermission = new EnhancedPermission();

    /**
     * 认证服务配置
     */
    private AuthService authService = new AuthService();

    /**
     * 需要权限验证的服务列表
     */
    private List<String> authRequiredServices = List.of("siae-auth", "siae-user", "siae-content", "siae-admin");

    /**
     * 白名单路径（不需要认证）
     */
    private List<String> whitelistPaths = List.of(
            "/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/refresh-token", "/api/v1/auth/validate-token",
        "/login", "/register", "/logout", "/refresh-token",
        "/actuator/**", "/swagger-ui/**", "/v3/api-docs/**",
        "/favicon.ico", "/error"
    );

    /**
     * JWT配置
     */
    @Data
    public static class Jwt {
        /**
         * 是否启用JWT认证
         */
        private boolean enabled = true;

        /**
         * JWT密钥
         */
        private String secret = "siae-default-secret-key-change-in-production";

        /**
         * 访问令牌过期时间（秒）
         */
        private long accessTokenExpiration = 7200; // 2小时

        /**
         * 刷新令牌过期时间（秒）
         */
        private long refreshTokenExpiration = 604800; // 7天

        /**
         * 令牌前缀
         */
        private String tokenPrefix = "Bearer ";

        /**
         * 请求头名称
         */
        private String headerName = "Authorization";

        /**
         * 是否允许多设备登录
         */
        private boolean allowMultipleDevices = true;

        /**
         * 令牌发行者
         */
        private String issuer = "siae-system";
    }

    /**
     * 权限配置
     */
    @Data
    public static class Permission {
        /**
         * 是否启用权限缓存
         */
        private boolean cacheEnabled = true;

        /**
         * 权限缓存过期时间（秒）
         */
        private long cacheExpiration = 1800; // 30分钟

        /**
         * 权限缓存键前缀
         */
        private String cacheKeyPrefix = "siae:permission:";

        /**
         * 是否启用Redis权限服务
         */
        private boolean redisEnabled = true;

        /**
         * Redis不可用时是否使用降级服务
         */
        private boolean fallbackEnabled = true;

        /**
         * 权限检查失败时是否抛出异常
         */
        private boolean throwExceptionOnFailure = true;

        /**
         * 是否启用权限日志
         */
        private boolean logEnabled = false;
    }

    /**
     * 判断当前服务是否需要权限验证
     * 
     * @param applicationName 应用名称
     * @return 是否需要权限验证
     */
    public boolean isAuthRequired(String applicationName) {
        return enabled && authRequiredServices.contains(applicationName);
    }

    /**
     * 判断路径是否在白名单中
     * 
     * @param path 请求路径
     * @return 是否在白名单中
     */
    public boolean isWhitelistPath(String path) {
        return whitelistPaths.stream().anyMatch(pattern ->
            path.matches(pattern.replace("**", ".*").replace("*", "[^/]*")));
    }

    /**
     * 增强权限控制配置
     */
    @Data
    public static class EnhancedPermission {

        /**
         * 是否启用增强权限控制
         */
        private boolean enabled = false;

        /**
         * 超级管理员角色名称
         */
        private String superAdminRole = "ROOT";

        /**
         * 是否启用全局超级管理员放行
         */
        private boolean globalSuperAdminEnabled = true;

        /**
         * 是否启用增强SpEL表达式
         */
        private boolean enhancedSpelEnabled = true;

        /**
         * 权限检查失败时是否记录详细日志
         */
        private boolean detailedLogging = true;

        /**
         * 是否启用权限缓存优化
         */
        private boolean cacheOptimizationEnabled = true;
    }

    /**
     * 认证服务配置
     */
    @Data
    public static class AuthService {

        /**
         * 认证服务基础URL
         */
        private String baseUrl = "http://siae-auth:8000";

        /**
         * 连接超时时间（毫秒）
         */
        private int connectTimeout = 3000;

        /**
         * 读取超时时间（毫秒）
         */
        private int readTimeout = 5000;

        /**
         * 是否启用token数据库验证
         */
        private boolean tokenValidationEnabled = true;

        /**
         * 验证失败时的重试次数
         */
        private int retryCount = 1;

        /**
         * 重试间隔时间（毫秒）
         */
        private int retryInterval = 1000;
    }
}
