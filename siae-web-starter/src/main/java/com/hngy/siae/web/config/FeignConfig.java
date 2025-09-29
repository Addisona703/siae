package com.hngy.siae.web.config;

import com.hngy.siae.core.config.AuthProperties;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign配置类
 * 注册认证拦截器
 *
 * 注意：不在这里使用 @EnableFeignClients，避免与服务自身的 @EnableFeignClients 冲突
 * 每个服务应该在自己的启动类中配置 @EnableFeignClients
 *
 * @author KEYKB
 */
@Configuration
// @EnableFeignClients(basePackages = "com.hngy.siae") // 移除，避免冲突
public class FeignConfig {

    /**
     * 注册认证拦截器
     * 会被所有使用了 siae-web-starter 的服务自动引入
     */
    @Bean
    public RequestInterceptor authenticationRequestInterceptor(AuthProperties authProperties) {
        return new FeignAuthenticationInterceptor(authProperties);
    }
}