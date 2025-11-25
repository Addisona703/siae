package com.hngy.siae.security.autoconfigure;

import com.hngy.siae.security.aop.SiaeAuthorizeAspect;
import com.hngy.siae.security.config.SimpleEnhancedPermissionConfig;
import com.hngy.siae.security.filter.ServiceAuthenticationFilter;
// import com.hngy.siae.security.filter.JwtAuthenticationFilter; // 旧版本，已废弃
// import com.hngy.siae.security.filter.ServiceInterCallFilter; // 旧版本，已废弃
import com.hngy.siae.security.handler.JsonAccessDeniedHandler;
import com.hngy.siae.security.handler.JsonAuthenticationEntryPoint;
import com.hngy.siae.security.properties.SecurityProperties;
import com.hngy.siae.security.service.impl.FallbackPermissionServiceImpl;
import com.hngy.siae.security.service.impl.RedisPermissionServiceImpl;
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
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.context.annotation.Lazy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;

/**
 * 安全功能自动配置类
 * 根据配置条件自动装配安全相关组件
 *
 * ✅ 已更新：使用新的 ServiceAuthenticationFilter（JWT网关优化方案）
 * ❌ 废弃：JwtAuthenticationFilter 和 ServiceInterCallFilter（旧版本）
 *
 * @author SIAE开发团队
 */
@Slf4j
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "siae.security", name = "enabled", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties(SecurityProperties.class)
@EnableWebSecurity
@Import({
    RedisPermissionServiceImpl.class,
    FallbackPermissionServiceImpl.class,
    ServiceAuthenticationFilter.class, // 新版本：优化的认证过滤器
    SimpleEnhancedPermissionConfig.class, // 方法级安全控制，根据 siae.security.enabled 自动启用
    SiaeAuthorizeAspect.class
})
public class SecurityAutoConfiguration {

    private final SecurityProperties securityProperties;
    private final ServiceAuthenticationFilter serviceAuthenticationFilter; // 新版本过滤器
    // private final JwtAuthenticationFilter jwtAuthenticationFilter; // 旧版本，已废弃
    // private final ServiceInterCallFilter serviceInterCallFilter; // 旧版本，已废弃
    private final ApplicationContext applicationContext;

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

    @Bean
    @ConditionalOnMissingBean
    public JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return new JsonAuthenticationEntryPoint(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public JsonAccessDeniedHandler jsonAccessDeniedHandler(ObjectMapper objectMapper) {
        return new JsonAccessDeniedHandler(objectMapper);
    }

    /**
     * 配置安全过滤器链（优化版本）
     * 使用新的 ServiceAuthenticationFilter 实现网关优化方案
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JsonAuthenticationEntryPoint authenticationEntryPoint,
                                           JsonAccessDeniedHandler accessDeniedHandler) throws Exception {
        log.info("配置安全过滤器链，应用: {}（使用优化版本）", applicationName);

        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));;

        // 关闭默认的formLogin，避免未登录自动跳转登录页
        http.formLogin(AbstractHttpConfigurer::disable);
        http.logout(AbstractHttpConfigurer::disable);

        // 自定义未认证/无权限处理，返回标准 JSON 响应
        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
        );

        if (securityProperties.isAuthRequired(applicationName)) {
            log.info("应用 {} 需要权限验证，配置认证规则（优化版本）", applicationName);
            configureAuthRequiredSecurity(http);
        } else {
            log.info("应用 {} 不需要权限验证，配置宽松规则", applicationName);
            configurePermissiveSecurity(http);
        }

        return http.build();
    }

    /**
     * 配置需要认证的安全规则（优化版本）
     * 使用新的 ServiceAuthenticationFilter
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

        // 添加优化的认证过滤器
        if (securityProperties.getJwt().isEnabled()) {
            http.addFilterBefore(serviceAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            log.info("✅ ServiceAuthenticationFilter（优化版本）已启用");
            log.info("功能：网关请求直接读取用户信息 + 权限查询 + 认证上下文填充");
        }

        /*
        // ❌ 旧版本过滤器已废弃，使用新的 ServiceAuthenticationFilter 替代
        // 添加JWT认证过滤器
        if (securityProperties.getJwt().isEnabled()) {
            http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            log.info("JWT认证过滤器已启用");

            // 添加通用服务间调用过滤器（在JWT过滤器之后）
            http.addFilterAfter(serviceInterCallFilter, jwtAuthenticationFilter.getClass());
            log.info("服务间调用过滤器已启用，执行顺序：JwtAuthenticationFilter -> ServiceInterCallFilter");
        }
        */

        // 添加特定服务的Token过滤器（如果存在）
        // 注意：大部分服务应该使用新的ServiceAuthenticationFilter，只有特殊需求的服务才需要自定义过滤器
//        configureServiceCallTokenFilter(http);
    }

    /**
     * 配置宽松的安全规则（用于不需要认证的服务）
     */
    private void configurePermissiveSecurity(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
    }

    /**
     * 配置特定服务的Token过滤器（已废弃）
     *
     * @deprecated 使用新的 ServiceAuthenticationFilter 统一处理所有认证场景
     */
    @Deprecated
    private void configureServiceCallTokenFilter(HttpSecurity http) throws Exception {
        /*
        try {
            // 尝试从应用上下文中获取ServiceCallTokenFilter Bean
            OncePerRequestFilter serviceCallTokenFilter = applicationContext.getBean("serviceCallTokenFilter", OncePerRequestFilter.class);

            // 将ServiceCallTokenFilter添加到ServiceInterCallFilter之后
            http.addFilterAfter(serviceCallTokenFilter, serviceInterCallFilter.getClass());

            log.info("特定服务Token过滤器已配置，执行顺序：JwtAuthenticationFilter -> ServiceInterCallFilter -> ServiceCallTokenFilter");

        } catch (Exception e) {
            // 如果没有找到ServiceCallTokenFilter Bean，则跳过配置
            log.debug("未找到特定服务Token过滤器Bean，跳过配置: {}", e.getMessage());
        }
        */
        log.debug("configureServiceCallTokenFilter 已废弃，使用 ServiceAuthenticationFilter 统一处理");
    }

    /**
     * RestTemplate配置（用于调用认证服务）
     * 使用@Lazy注解打破循环依赖
     */
    @Bean("authServiceRestTemplate")
    @Lazy  // 延迟初始化，打破循环依赖
    @ConditionalOnProperty(prefix = "siae.security.auth-service", name = "token-validation-enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(name = "authServiceRestTemplate")
    public RestTemplate authServiceRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // 设置连接超时时间
        factory.setConnectTimeout(securityProperties.getAuthService().getConnectTimeout());

        // 设置读取超时时间
        factory.setReadTimeout(securityProperties.getAuthService().getReadTimeout());

        RestTemplate restTemplate = new RestTemplate(factory);

        log.info("认证服务RestTemplate配置完成: connectTimeout={}ms, readTimeout={}ms",
                securityProperties.getAuthService().getConnectTimeout(),
                securityProperties.getAuthService().getReadTimeout());

        return restTemplate;
    }

    public SecurityAutoConfiguration(SecurityProperties securityProperties,
                                   @Lazy ServiceAuthenticationFilter serviceAuthenticationFilter, // 新版本过滤器
                                   ApplicationContext applicationContext) {
        this.securityProperties = securityProperties;
        this.serviceAuthenticationFilter = serviceAuthenticationFilter; // 新版本过滤器
        this.applicationContext = applicationContext;

        log.info("SIAE Security Starter 自动配置已启用（优化版本）");
        log.info("认证策略: 网关验签 + 服务填充");
        log.info("JWT认证: {}", securityProperties.getJwt().isEnabled() ? "启用" : "禁用");
        log.info("权限缓存: {}", securityProperties.getPermission().isCacheEnabled() ? "启用" : "禁用");
        log.info("Redis权限服务: {}", securityProperties.getPermission().isRedisEnabled() ? "启用" : "禁用");
        log.info("权限降级服务: {}", securityProperties.getPermission().isFallbackEnabled() ? "启用" : "禁用");
        log.info("简化增强权限控制: {}", securityProperties.getEnhancedPermission().isEnabled() ? "启用" : "禁用");
        log.info("SiaeAuthorize注解支持: 启用");
        log.info("✅ ServiceAuthenticationFilter: 启用（支持网关请求、内部调用、直接访问）");
    }
}
