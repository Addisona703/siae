package com.hngy.siae.notification.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * 邮件配置类
 *
 * @author KEYKB
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.mail", name = "host")
public class MailConfig {
    // Spring Boot 会自动配置 JavaMailSender
    // 这个类只是用来确保只在配置了邮件服务器时才启用邮件功能
}