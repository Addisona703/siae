package com.hngy.siae.attendance.annotation;

import java.lang.annotation.*;

/**
 * 限流注解
 * 用于标记需要限流的接口
 *
 * @author SIAE Team
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流key前缀
     */
    String key() default "rate_limit";

    /**
     * 时间窗口内允许的请求次数
     * 默认值为-1，表示使用配置文件中的值
     */
    int permits() default -1;

    /**
     * 时间窗口（秒）
     * 默认值为-1，表示使用配置文件中的值
     */
    int window() default -1;

    /**
     * 限流类型
     */
    RateLimitType type() default RateLimitType.USER;

    /**
     * 限流类型枚举
     */
    enum RateLimitType {
        /**
         * 按用户限流
         */
        USER,

        /**
         * 按IP限流
         */
        IP,

        /**
         * 全局限流
         */
        GLOBAL
    }
}
