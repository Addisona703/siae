package com.hngy.siae.security.autoconfigure;

import com.hngy.siae.security.properties.SecurityProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * SecurityProperties 自动配置类
 * 
 * 独立的配置类，确保 SecurityProperties 始终被注册
 * 即使 siae.security.enabled=false 时也能正常工作
 * 
 * @author SIAE开发团队
 */
@AutoConfiguration
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityPropertiesAutoConfiguration {
    // 这个类只负责注册 SecurityProperties Bean
    // 不需要其他配置
}
