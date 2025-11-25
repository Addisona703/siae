package com.hngy.siae.attendance.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 限流配置
 *
 * @author SIAE Team
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "attendance.rate-limit")
public class RateLimitConfig {

    /**
     * 是否启用限流
     */
    private boolean enabled = true;

    /**
     * 签到接口限流配置
     */
    private RateLimitRule checkIn = new RateLimitRule(10, 60); // 每分钟10次

    /**
     * 签退接口限流配置
     */
    private RateLimitRule checkOut = new RateLimitRule(10, 60); // 每分钟10次

    /**
     * 查询接口限流配置
     */
    private RateLimitRule query = new RateLimitRule(100, 60); // 每分钟100次

    /**
     * 导出接口限流配置
     */
    private RateLimitRule export = new RateLimitRule(5, 60); // 每分钟5次

    /**
     * 限流规则
     */
    @Data
    public static class RateLimitRule {
        /**
         * 允许的请求次数
         */
        private int permits;

        /**
         * 时间窗口（秒）
         */
        private int window;

        public RateLimitRule() {
        }

        public RateLimitRule(int permits, int window) {
            this.permits = permits;
            this.window = window;
        }
    }
}
