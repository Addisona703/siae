package com.hngy.siae.security.autoconfigure;

import com.hngy.siae.security.aop.SiaeAuthorizeAspect;
import com.hngy.siae.security.config.SimpleEnhancedPermissionConfig;
import com.hngy.siae.security.filter.JwtAuthenticationFilter;
import com.hngy.siae.security.properties.SecurityProperties;
import com.hngy.siae.security.service.impl.FallbackPermissionServiceImpl;
import com.hngy.siae.security.service.impl.RedisPermissionServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 安全功能自动配置类
 * 根据配置条件自动装配安全相关组件
 * 
 * @author SIAE开发团队
 */
@Slf4j
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "siae.security", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SecurityProperties.class)
@EnableWebSecurity
@Import({
    RedisPermissionServiceImpl.class,
    FallbackPermissionServiceImpl.class,
    JwtAuthenticationFilter.class,
    SimpleEnhancedPermissionConfig.class,
    SiaeAuthorizeAspect.class
})
public class SecurityAutoConfiguration {

    private final SecurityProperties securityProperties;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${spring.application.name:unknown}")
    private String applicationName;

    /**
     * 配置默认密码编码器
     * 使用BCrypt算法进行密码加密，提供良好的安全性
     *
     * @return BCryptPasswordEncoder实例
     */
    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
        log.info("配置默认密码编码器: BCryptPasswordEncoder");
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置安全过滤器链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("配置安全过滤器链，应用: {}", applicationName);

        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions().deny());

        // 关闭默认的formLogin，避免未登录自动跳转登录页
        http.formLogin(AbstractHttpConfigurer::disable);
        http.logout(AbstractHttpConfigurer::disable);

        // 自定义未认证处理，返回401 JSON，不跳转
        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"message\":\"未认证，请先登录\"}");
                })
        );

        if (securityProperties.isAuthRequired(applicationName)) {
            log.info("应用 {} 需要权限验证，配置认证规则", applicationName);
            configureAuthRequiredSecurity(http);
        } else {
            log.info("应用 {} 不需要权限验证，配置宽松规则", applicationName);
            configurePermissiveSecurity(http);
        }

        return http.build();
    }


    /**
     * 配置需要认证的安全规则
     */
    private void configureAuthRequiredSecurity(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authz -> {
            // 白名单路径
            String[] whitelistPaths = securityProperties.getWhitelistPaths().toArray(new String[0]);
            authz.requestMatchers(whitelistPaths).permitAll();

            // 管理员接口
//            authz.requestMatchers("/api/v1/**").hasRole("ROOT");
            // 其他路径需要认证
            authz.anyRequest().authenticated();
        });

        // 添加JWT认证过滤器
        if (securityProperties.getJwt().isEnabled()) {
            http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            log.info("JWT认证过滤器已启用");
        }
    }

    /**
     * 配置宽松的安全规则（用于不需要认证的服务）
     */
    private void configurePermissiveSecurity(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
    }

    public SecurityAutoConfiguration(SecurityProperties securityProperties, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.securityProperties = securityProperties;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        
        log.info("SIAE Security Starter 自动配置已启用");
        log.info("JWT认证: {}", securityProperties.getJwt().isEnabled() ? "启用" : "禁用");
        log.info("权限缓存: {}", securityProperties.getPermission().isCacheEnabled() ? "启用" : "禁用");
        log.info("Redis权限服务: {}", securityProperties.getPermission().isRedisEnabled() ? "启用" : "禁用");
        log.info("权限降级服务: {}", securityProperties.getPermission().isFallbackEnabled() ? "启用" : "禁用");
        log.info("简化增强权限控制: {}", securityProperties.getEnhancedPermission().isEnabled() ? "启用" : "禁用");
        log.info("SiaeAuthorize注解支持: 启用");
    }
}
