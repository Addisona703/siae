package com.hngy.siae.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开启统一响应包装器的开关
 * Custom annotation to enable unified response wrapping.
 * Apply this to a class or method to have its return value automatically
 * wrapped in a Result<T> object.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD}) // Can be applied to classes or methods
public @interface UnifiedResponse {
}
