package com.hngy.siae.media.security;

import com.hngy.siae.media.domain.enums.AuditAction;

import java.lang.annotation.*;

/**
 * 审计日志注解
 * 用于自动记录审计日志
 *
 * @author SIAE Team
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /**
     * 操作类型
     */
    AuditAction action();

    /**
     * 操作描述
     */
    String description() default "";

}
