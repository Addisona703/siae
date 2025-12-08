package com.hngy.siae.feign.autoconfigure;

import com.hngy.siae.core.config.AuthProperties;
import com.hngy.siae.feign.decoder.ResultErrorDecoder;
import com.hngy.siae.feign.decoder.ResultUnwrapDecoder;
import com.hngy.siae.feign.interceptor.FeignAuthenticationInterceptor;
import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;

/**
 * Feign 自动配置类
 * <p>
 * 提供 Feign 客户端的自动配置，包括：
 * <ul>
 *     <li>ResultUnwrapDecoder - 自动解包 Result&lt;T&gt; 对象</li>
 *     <li>ResultErrorDecoder - 自动转换错误响应为业务异常</li>
 *     <li>日志级别配置</li>
 * </ul>
 *
 * @author SIAE开发团队
 */
@AutoConfiguration
@ConditionalOnClass({Decoder.class, ErrorDecoder.class})
@ConditionalOnProperty(prefix = "siae.feign", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(FeignProperties.class)
public class FeignAutoConfiguration {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(FeignAutoConfiguration.class);

    private final FeignProperties properties;

    public FeignAutoConfiguration(FeignProperties properties) {
        this.properties = properties;
        log.info("[SIAE-Feign] Feign auto-configuration initialized: unwrapResult={}, errorDecoder={}, logLevel={}",
                properties.isUnwrapResult(), properties.isErrorDecoder(), properties.getLogLevel());
    }

    /**
     * 配置 Feign 解码器
     * <p>
     * 使用 ResultUnwrapDecoder 自动解包 Result&lt;T&gt; 对象
     *
     * @param messageConverters HTTP 消息转换器工厂
     * @return Feign 解码器
     */
    @Bean
    @ConditionalOnMissingBean(Decoder.class)
    @ConditionalOnProperty(prefix = "siae.feign", name = "unwrap-result", havingValue = "true", matchIfMissing = true)
    public Decoder feignDecoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        log.debug("[SIAE-Feign] Registering ResultUnwrapDecoder");
        return new ResultUnwrapDecoder(new SpringDecoder(messageConverters));
    }

    /**
     * 配置 Feign 错误解码器
     * <p>
     * 使用 ResultErrorDecoder 自动转换错误响应为业务异常
     *
     * @return Feign 错误解码器
     */
    @Bean
    @ConditionalOnMissingBean(ErrorDecoder.class)
    @ConditionalOnProperty(prefix = "siae.feign", name = "error-decoder", havingValue = "true", matchIfMissing = true)
    public ErrorDecoder feignErrorDecoder() {
        log.debug("[SIAE-Feign] Registering ResultErrorDecoder");
        return new ResultErrorDecoder();
    }

    /**
     * 配置 Feign 日志级别
     *
     * @return Feign 日志级别
     */
    @Bean
    @ConditionalOnMissingBean(Logger.Level.class)
    public Logger.Level feignLoggerLevel() {
        String level = properties.getLogLevel();
        Logger.Level logLevel;
        try {
            logLevel = Logger.Level.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("[SIAE-Feign] Invalid log level '{}', using BASIC", level);
            logLevel = Logger.Level.BASIC;
        }
        log.debug("[SIAE-Feign] Feign log level set to: {}", logLevel);
        return logLevel;
    }

    /**
     * 配置 Feign 认证拦截器
     * <p>
     * 自动为服务间调用添加认证头，包括：
     * <ul>
     *     <li>X-Internal-Service-Call - 内部服务调用密钥</li>
     *     <li>X-Caller-Service - 调用方服务名</li>
     *     <li>X-Call-Timestamp - 调用时间戳</li>
     *     <li>X-On-Behalf-Of-User - 代理用户ID（如果有）</li>
     * </ul>
     *
     * @param authProperties 认证配置
     * @param applicationName 当前应用名称
     * @return Feign 认证拦截器
     */
    @Bean
    @ConditionalOnMissingBean(name = "feignAuthenticationInterceptor")
    @ConditionalOnProperty(prefix = "siae.feign", name = "auth-interceptor", havingValue = "true", matchIfMissing = true)
    public RequestInterceptor feignAuthenticationInterceptor(
            AuthProperties authProperties,
            @Value("${spring.application.name:unknown}") String applicationName) {
        log.debug("[SIAE-Feign] Registering FeignAuthenticationInterceptor for service: {}", applicationName);
        return new FeignAuthenticationInterceptor(authProperties, applicationName);
    }
}
