package com.hngy.siae.common.configs.swaggerConfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Swagger自动配置类
 * 
 * 根据配置属性自动启用或禁用Swagger配置
 * 
 * @author SIAE开发团队
 */
@Configuration
@ConditionalOnProperty(
    prefix = "springdoc.api-docs", 
    name = "enabled", 
    havingValue = "true", 
    matchIfMissing = true
)
@Import({
    OpenApiConfig.class,
    SwaggerUIConfig.class,
    SwaggerProperties.class
})
public class SwaggerAutoConfiguration {

    /**
     * 自动配置类，无需额外实现
     * 通过@Import注解导入所有必要的配置类
     * 通过@ConditionalOnProperty注解控制是否启用
     */
}
