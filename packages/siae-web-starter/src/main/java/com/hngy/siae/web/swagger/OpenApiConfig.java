package com.hngy.siae.web.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.hngy.siae.web.swagger.SwaggerConstants.*;

/**
 * 统一的OpenAPI配置类
 * <p>
 * 整合三个微服务的Swagger配置，支持多服务分组显示
 *
 * @author SIAE开发团队
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:siae-service}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    /**
     * 全局OpenAPI配置
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(buildApiInfo())
                .servers(buildServers())
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                .addSecurityItem(new SecurityRequirement().addList("ApiKey"))
                .components(buildComponents());
    }

    /**
     * 认证服务API分组
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group(AUTH_GROUP)
                .displayName(AUTH_GROUP_DISPLAY)
                .pathsToMatch(AUTH_PATHS)
                .packagesToScan(AUTH_PACKAGE)
                .build();
    }

    /**
     * 用户服务API分组
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group(USER_GROUP)
                .displayName(USER_GROUP_DISPLAY)
                .pathsToMatch(USER_PATHS)
                .packagesToScan(USER_PACKAGE)
                .build();
    }

    /**
     * 内容服务API分组
     */
    @Bean
    public GroupedOpenApi contentApi() {
        return GroupedOpenApi.builder()
                .group(CONTENT_GROUP)
                .displayName(CONTENT_GROUP_DISPLAY)
                .pathsToMatch(CONTENT_PATHS)
                .packagesToScan(CONTENT_PACKAGE)
                .build();
    }

    /**
     * 通知服务API分组
     */
    @Bean
    public GroupedOpenApi notificationApi() {
        return GroupedOpenApi.builder()
                .group(NOTIFICATION_GROUP)
                .displayName(NOTIFICATION_GROUP_DISPLAY)
                .pathsToMatch(NOTIFICATION_PATHS)
                .packagesToScan(NOTIFICATION_PACKAGE)
                .build();
    }

    /**
     * 系统管理API分组
     */
    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group(SYSTEM_GROUP)
                .displayName(SYSTEM_GROUP_DISPLAY)
                .pathsToMatch(SYSTEM_PATHS)
                .packagesToScan(AUTH_PACKAGE)
                .build();
    }

    /**
     * 构建API信息
     */
    private Info buildApiInfo() {
        String title = getServiceTitle();
        String description = getServiceDescription();

        return new Info()
                .title(title)
                .description(description)
                .version(DEFAULT_VERSION)
                .contact(new Contact()
                        .name(DEFAULT_CONTACT_NAME)
                        .email(DEFAULT_CONTACT_EMAIL)
                        .url(DEFAULT_CONTACT_URL))
                .license(new License()
                        .name(DEFAULT_LICENSE_NAME)
                        .url(DEFAULT_LICENSE_URL));
    }

    /**
     * 构建服务器信息
     */
    private List<Server> buildServers() {
        String localUrl = buildLocalUrl();
        String prodUrl = buildProdUrl();

        return List.of(
                new Server()
                        .url(localUrl)
                        .description(LOCAL_ENV_DESC),
                new Server()
                        .url(prodUrl)
                        .description(PROD_ENV_DESC),
                new Server()
                        .url(GATEWAY_URL)
                        .description(GATEWAY_ENV_DESC)
        );
    }

    /**
     * 构建安全组件
     */
    private Components buildComponents() {
        return new Components()
                .addSecuritySchemes(JWT_SCHEME_NAME, new SecurityScheme()
                        .name("Authorization")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description(JWT_DESCRIPTION))
                .addSecuritySchemes(API_KEY_SCHEME_NAME, new SecurityScheme()
                        .name(API_KEY_HEADER_NAME)
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .description(API_KEY_DESCRIPTION));
    }

    /**
     * 根据应用名称获取服务标题
     */
    private String getServiceTitle() {
        return switch (applicationName) {
            case AUTH_SERVICE_NAME -> "SIAE认证服务API";
            case USER_SERVICE_NAME -> "SIAE用户服务API";
            case CONTENT_SERVICE_NAME -> "SIAE内容服务API";
            case NOTIFICATION_SERVICE_NAME -> "SIAE通知服务API";
            case GATEWAY_SERVICE_NAME -> "SIAE网关聚合API";
            default -> "SIAE微服务API";
        };
    }

    /**
     * 根据应用名称获取服务描述
     */
    private String getServiceDescription() {
        return switch (applicationName) {
            case AUTH_SERVICE_NAME -> AUTH_SERVICE_DESC;
            case USER_SERVICE_NAME -> USER_SERVICE_DESC;
            case CONTENT_SERVICE_NAME -> CONTENT_SERVICE_DESC;
            case NOTIFICATION_SERVICE_NAME -> NOTIFICATION_SERVICE_DESC;
            case GATEWAY_SERVICE_NAME -> GATEWAY_SERVICE_DESC;
            default -> DEFAULT_SERVICE_DESC;
        };
    }

    /**
     * 构建本地URL
     */
    private String buildLocalUrl() {
        String baseUrl = "http://localhost:" + serverPort;
        if (!"/".equals(contextPath)) {
            baseUrl += contextPath;
        }
        return baseUrl;
    }

    /**
     * 构建生产URL
     */
    private String buildProdUrl() {
        return switch (applicationName) {
            case AUTH_SERVICE_NAME -> PROD_BASE_URL + "/api/v1/auth";
            case USER_SERVICE_NAME -> PROD_BASE_URL + "/api/v1/user";
            case CONTENT_SERVICE_NAME -> PROD_BASE_URL + "/api/v1/content";
            case NOTIFICATION_SERVICE_NAME -> PROD_BASE_URL + "/api/v1/notification";
            default -> PROD_BASE_URL;
        };
    }
}
