package com.hngy.siae.web.autoconfigure;

import com.hngy.siae.web.advice.GlobalExceptionHandler;
import com.hngy.siae.web.advice.UnifiedResponseAdvice;
import com.hngy.siae.web.config.JacksonConfig;
import com.hngy.siae.web.config.MybatisPlusConfig;
import com.hngy.siae.web.properties.WebProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * Web功能自动配置类
 * 根据配置条件自动装配Web相关组件
 * 
 * @author SIAE开发团队
 */
@Slf4j
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(WebProperties.class)
@Import({
    UnifiedResponseAdvice.class,
    GlobalExceptionHandler.class,
    JacksonConfig.class,
    MybatisPlusConfig.class
})
public class WebAutoConfiguration {

    public WebAutoConfiguration(WebProperties webProperties) {
        log.info("SIAE Web Starter 自动配置已启用");
        log.info("统一响应处理: {}", webProperties.getResponse().isEnabled() ? "启用" : "禁用");
        log.info("全局异常处理: {}", webProperties.getException().isEnabled() ? "启用" : "禁用");
        log.info("Jackson配置: {}", webProperties.getJackson().isEnabled() ? "启用" : "禁用");
        log.info("MyBatis Plus配置: {}", webProperties.getMybatisPlus().isEnabled() ? "启用" : "禁用");
    }
}
