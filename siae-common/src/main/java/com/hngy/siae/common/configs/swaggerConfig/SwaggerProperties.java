package com.hngy.siae.common.configs.swaggerConfig;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Swagger配置属性类
 *
 * 用于管理Swagger相关的配置参数，支持纯Java配置
 *
 * @author SIAE开发团队
 */
@Data
@Component
public class SwaggerProperties {

    /**
     * 是否启用Swagger
     */
    private boolean enabled = true;

    /**
     * API信息配置
     */
    private ApiInfo apiInfo = new ApiInfo();

    /**
     * 联系人信息配置
     */
    private Contact contact = new Contact();

    /**
     * 许可证信息配置
     */
    private License license = new License();

    /**
     * 服务器配置列表
     */
    private List<Server> servers = List.of();

    /**
     * 安全配置
     */
    private Security security = new Security();

    /**
     * UI配置
     */
    private UI ui = new UI();

    /**
     * API信息配置类
     */
    @Data
    public static class ApiInfo {
        /**
         * API标题
         */
        private String title = "SIAE微服务API";

        /**
         * API描述
         */
        private String description = "软件协会官网系统API文档";

        /**
         * API版本
         */
        private String version = "v1.0.0";

        /**
         * 服务条款URL
         */
        private String termsOfService = "";
    }

    /**
     * 联系人信息配置类
     */
    @Data
    public static class Contact {
        /**
         * 联系人姓名
         */
        private String name = "SIAE开发团队";

        /**
         * 联系人邮箱
         */
        private String email = "dev@siae.com";

        /**
         * 联系人URL
         */
        private String url = "https://github.com/siae";
    }

    /**
     * 许可证信息配置类
     */
    @Data
    public static class License {
        /**
         * 许可证名称
         */
        private String name = "MIT License";

        /**
         * 许可证URL
         */
        private String url = "https://opensource.org/licenses/MIT";
    }

    /**
     * 服务器配置类
     */
    @Data
    public static class Server {
        /**
         * 服务器URL
         */
        private String url;

        /**
         * 服务器描述
         */
        private String description;
    }

    /**
     * 安全配置类
     */
    @Data
    public static class Security {
        /**
         * 是否启用JWT认证
         */
        private boolean jwtEnabled = true;

        /**
         * JWT认证方案名称
         */
        private String jwtSchemeName = "JWT";

        /**
         * JWT认证描述
         */
        private String jwtDescription = "JWT认证，请在请求头中添加：Authorization: Bearer {token}";

        /**
         * 是否启用API Key认证
         */
        private boolean apiKeyEnabled = true;

        /**
         * API Key认证方案名称
         */
        private String apiKeySchemeName = "ApiKey";

        /**
         * API Key请求头名称
         */
        private String apiKeyHeaderName = "X-API-KEY";

        /**
         * API Key认证描述
         */
        private String apiKeyDescription = "API密钥认证，用于服务间调用";
    }

    /**
     * UI配置类
     */
    @Data
    public static class UI {
        /**
         * 是否启用Swagger UI
         */
        private boolean enabled = true;

        /**
         * Swagger UI路径
         */
        private String path = "/swagger-ui.html";

        /**
         * 标签排序方式 (alpha: 字母排序, method: HTTP方法排序)
         */
        private String tagsSorter = "alpha";

        /**
         * 操作排序方式 (alpha: 字母排序, method: HTTP方法排序)
         */
        private String operationsSorter = "alpha";

        /**
         * 是否显示请求持续时间
         */
        private boolean showRequestDuration = true;

        /**
         * 是否显示通用错误响应
         */
        private boolean showCommonResponses = true;

        /**
         * 默认展开深度
         */
        private int defaultModelsExpandDepth = 1;

        /**
         * 默认模型渲染深度
         */
        private int defaultModelExpandDepth = 1;

        /**
         * 是否显示扩展
         */
        private boolean showExtensions = true;

        /**
         * 是否显示通用扩展
         */
        private boolean showCommonExtensions = true;
    }
}
