package com.hngy.siae.ai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 异步配置
 * 启用@Async注解支持，用于会话异步持久化
 *
 * @author SIAE Team
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
