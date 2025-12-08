package com.hngy.siae.content.strategy.audit;

import com.hngy.siae.content.enums.TypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审核类型注解
 * 用于标记审核处理器实现类对应的目标类型
 *
 * @author Kiro
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditType {
    TypeEnum value();
}
