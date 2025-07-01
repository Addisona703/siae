package com.hngy.siae.content.strategy;


import com.hngy.siae.content.common.enums.ContentTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 策略类型
 *
 * @author KEYKB
 * 创建时间: 2025/05/19
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface StrategyType {
    ContentTypeEnum value();
}

