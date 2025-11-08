package com.hngy.siae.media.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * 优雅关闭配置
 * 
 * 优雅关闭（Graceful Shutdown）确保应用停止时：
 * 1. 停止接收新请求
 * 2. 等待正在处理的请求完成（最多等待配置的超时时间）
 * 3. 关闭所有资源（数据库连接、消息队列等）
 * 4. 最后才真正退出应用
 * 
 * 配置方式：
 * 在 application.yml 中配置：
 * server:
 *   shutdown: graceful
 * spring:
 *   lifecycle:
 *     timeout-per-shutdown-phase: 30s
 * 
 * 注意：
 * - Spring Boot 2.3+ 已内置优雅关闭支持
 * - 不需要额外的 Bean 配置
 * - 只需在配置文件中启用即可
 */
@Slf4j
@Configuration
@Deprecated
public class GracefulShutdownConfig {
    
    // Spring Boot 2.3+ 已内置优雅关闭支持
    // 只需在 application.yml 中配置 server.shutdown=graceful 即可
    // 此配置类保留用于未来可能的自定义扩展
    
}
