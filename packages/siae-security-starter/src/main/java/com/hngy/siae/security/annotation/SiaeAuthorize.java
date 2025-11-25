package com.hngy.siae.security.annotation;

import org.intellij.lang.annotations.Language;

import java.lang.annotation.*;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SiaeAuthorize {
    /**
     * SpEL 表达式，用来描述权限规则
     * 例如：hasRole('ADMIN') or hasAuthority('LOG_VIEW')
     */
//    @Language("SpEL")
    String value() default "";
}
