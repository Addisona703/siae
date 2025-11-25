package com.hngy.siae.attendance.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 
 * <p>用于标记需要记录操作日志的方法</p>
 * 
 * @author SIAE Team
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 操作类型
     * 
     * @return 操作类型，如：CREATE, UPDATE, DELETE, APPROVE等
     */
    String type();

    /**
     * 操作模块
     * 
     * @return 操作模块，如：ATTENDANCE, LEAVE, ANOMALY, RULE等
     */
    String module();

    /**
     * 操作描述
     * 
     * @return 操作描述，支持SpEL表达式
     */
    String description() default "";

    /**
     * 是否记录请求参数
     * 
     * @return true表示记录，false表示不记录
     */
    boolean recordParams() default true;

    /**
     * 是否记录响应结果
     * 
     * @return true表示记录，false表示不记录
     */
    boolean recordResult() default false;
}
