package com.hngy.siae.web.swagger;

/**
 * Swagger配置常量类
 * 
 * 集中管理所有Swagger相关的配置常量
 * 
 * @author SIAE开发团队
 */
public final class SwaggerConstants {

    private SwaggerConstants() {
        // 工具类，禁止实例化
    }

    // ==================== 基础信息常量 ====================
    
    /** 默认API版本 */
    public static final String DEFAULT_VERSION = "v1.0.0";
    
    /** 默认联系人名称 */
    public static final String DEFAULT_CONTACT_NAME = "SIAE开发团队";
    
    /** 默认联系人邮箱 */
    public static final String DEFAULT_CONTACT_EMAIL = "3183389935@qq.com";
    
    /** 默认联系人URL */
    public static final String DEFAULT_CONTACT_URL = "https://github.com/siae";
    
    /** 默认许可证名称 */
    public static final String DEFAULT_LICENSE_NAME = "MIT License";
    
    /** 默认许可证URL */
    public static final String DEFAULT_LICENSE_URL = "https://opensource.org/licenses/MIT";

    // ==================== 服务信息常量 ====================
    
    /** 认证服务名称 */
    public static final String AUTH_SERVICE_NAME = "siae-auth";
    
    /** 用户服务名称 */
    public static final String USER_SERVICE_NAME = "siae-user";
    
    /** 内容服务名称 */
    public static final String CONTENT_SERVICE_NAME = "siae-content";
    
    /** 通知服务名称 */
    public static final String NOTIFICATION_SERVICE_NAME = "siae-notification";
    
    /** 网关服务名称 */
    public static final String GATEWAY_SERVICE_NAME = "siae-gateway";

    // ==================== API分组常量 ====================
    
    /** 认证服务分组 */
    public static final String AUTH_GROUP = "01-认证服务";
    public static final String AUTH_GROUP_DISPLAY = "🔐 认证服务API";
    
    /** 用户服务分组 */
    public static final String USER_GROUP = "02-用户服务";
    public static final String USER_GROUP_DISPLAY = "👥 用户服务API";
    
    /** 内容服务分组 */
    public static final String CONTENT_GROUP = "03-内容服务";
    public static final String CONTENT_GROUP_DISPLAY = "📝 内容服务API";
    
    /** 通知服务分组 */
    public static final String NOTIFICATION_GROUP = "04-通知服务";
    public static final String NOTIFICATION_GROUP_DISPLAY = "🔔 通知服务API";
    
    /** 系统管理分组 */
    public static final String SYSTEM_GROUP = "05-系统管理";
    public static final String SYSTEM_GROUP_DISPLAY = "⚙️ 系统管理API";

    // ==================== 路径匹配常量 ====================
    
    /** 认证服务路径 */
    public static final String[] AUTH_PATHS = {
        "/api/v1/auth/**", "/login", "/register", "/logout", "/refresh-token"
    };
    
    /** 用户服务路径 */
    public static final String[] USER_PATHS = {
        "/api/v1/user/**", "/users/**", "/user-profiles/**", "/members/**",
        "/member-candidates/**", "/classes/**", "/award-types/**",
        "/award-levels/**", "/user-awards/**"
    };
    
    /** 内容服务路径 */
    public static final String[] CONTENT_PATHS = {
        "/api/v1/content/**", "/contents/**", "/categories/**", "/tags/**",
        "/comments/**", "/audits/**", "/interactions/**", "/statistics/**"
    };
    
    /** 通知服务路径 */
    public static final String[] NOTIFICATION_PATHS = {
        "/api/v1/notification/**", "/notifications/**", "/email/**"
    };
    
    /** 系统管理路径 */
    public static final String[] SYSTEM_PATHS = {
        "/permissions/**", "/roles/**", "/user-role/**", "/user-permission/**", "/logs/**"
    };

    // ==================== 包扫描常量 ====================
    
    /** 认证服务包路径 */
    public static final String AUTH_PACKAGE = "com.hngy.siae.auth.controller";
    
    /** 用户服务包路径 */
    public static final String USER_PACKAGE = "com.hngy.siae.user.controller";
    
    /** 内容服务包路径 */
    public static final String CONTENT_PACKAGE = "com.hngy.siae.content.controller";
    
    /** 通知服务包路径 */
    public static final String NOTIFICATION_PACKAGE = "com.hngy.siae.notification.controller";

    // ==================== 安全认证常量 ====================
    
    /** JWT认证方案名称 */
    public static final String JWT_SCHEME_NAME = "JWT";
    
    /** JWT认证描述 */
    public static final String JWT_DESCRIPTION = "JWT认证，请在请求头中添加：Authorization: Bearer {token}";
    
    /** API Key认证方案名称 */
    public static final String API_KEY_SCHEME_NAME = "ApiKey";
    
    /** API Key请求头名称 */
    public static final String API_KEY_HEADER_NAME = "X-API-KEY";
    
    /** API Key认证描述 */
    public static final String API_KEY_DESCRIPTION = "API密钥认证，用于服务间调用";

    // ==================== 服务器URL常量 ====================
    
    /** 本地开发环境描述 */
    public static final String LOCAL_ENV_DESC = "本地开发环境";
    
    /** 生产环境描述 */
    public static final String PROD_ENV_DESC = "生产环境";
    
    /** 网关统一入口描述 */
    public static final String GATEWAY_ENV_DESC = "网关统一入口";
    
    /** 生产环境基础URL */
    public static final String PROD_BASE_URL = "https://api.siae.com";
    
    /** 网关URL */
    public static final String GATEWAY_URL = "http://localhost:8080";

    // ==================== 响应示例常量 ====================
    
    /** 成功响应示例 */
    public static final String SUCCESS_EXAMPLE = """
            {
              "code": 200,
              "message": "操作成功",
              "data": {},
              "timestamp": "2024-01-01T12:00:00"
            }
            """;
    
    /** 参数错误响应示例 */
    public static final String BAD_REQUEST_EXAMPLE = """
            {
              "code": 400,
              "message": "请求参数错误",
              "data": null,
              "timestamp": "2024-01-01T12:00:00"
            }
            """;
    
    /** 未授权响应示例 */
    public static final String UNAUTHORIZED_EXAMPLE = """
            {
              "code": 401,
              "message": "未授权访问，请先登录",
              "data": null,
              "timestamp": "2024-01-01T12:00:00"
            }
            """;
    
    /** 权限不足响应示例 */
    public static final String FORBIDDEN_EXAMPLE = """
            {
              "code": 403,
              "message": "权限不足，无法访问该资源",
              "data": null,
              "timestamp": "2024-01-01T12:00:00"
            }
            """;
    
    /** 资源不存在响应示例 */
    public static final String NOT_FOUND_EXAMPLE = """
            {
              "code": 404,
              "message": "请求的资源不存在",
              "data": null,
              "timestamp": "2024-01-01T12:00:00"
            }
            """;
    
    /** 服务器错误响应示例 */
    public static final String INTERNAL_ERROR_EXAMPLE = """
            {
              "code": 500,
              "message": "服务器内部错误，请稍后重试",
              "data": null,
              "timestamp": "2024-01-01T12:00:00"
            }
            """;

    // ==================== 服务描述常量 ====================
    
    /** 基础描述前缀 */
    public static final String BASE_DESCRIPTION = "软件协会官网系统 - ";
    
    /** 认证服务描述 */
    public static final String AUTH_SERVICE_DESC = BASE_DESCRIPTION + "认证授权服务API文档，提供用户登录、注册、权限管理等功能";
    
    /** 用户服务描述 */
    public static final String USER_SERVICE_DESC = BASE_DESCRIPTION + "用户服务API文档，提供用户信息管理、成员管理、班级管理等功能";
    
    /** 内容服务描述 */
    public static final String CONTENT_SERVICE_DESC = BASE_DESCRIPTION + "内容服务API文档，提供内容发布、分类管理、标签管理、评论管理等功能";
    
    /** 通知服务描述 */
    public static final String NOTIFICATION_SERVICE_DESC = BASE_DESCRIPTION + "通知服务API文档，提供邮件发送、站内通知、短信推送等功能";
    
    /** 网关服务描述 */
    public static final String GATEWAY_SERVICE_DESC = BASE_DESCRIPTION + "网关聚合API文档，统一展示所有微服务接口";
    
    /** 默认服务描述 */
    public static final String DEFAULT_SERVICE_DESC = BASE_DESCRIPTION + "微服务API文档";
}
