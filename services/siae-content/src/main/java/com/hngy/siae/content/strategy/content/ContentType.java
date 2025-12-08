package com.hngy.siae.content.strategy.content;

import com.hngy.siae.content.enums.ContentTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 策略类型注解
 * 用于标记内容策略实现类对应的内容类型
 *
 * @author KEYKB
 * 创建时间: 2025/05/19
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ContentType {
    ContentTypeEnum value();
}
