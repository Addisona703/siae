package com.hngy.siae.content.common.utils;

import com.hngy.siae.core.asserts.AssertUtils;

/**
 * 断言工具类适配器
 * 用于将旧的 AssertUtil 调用转发到 common 模块的 AssertUtils
 * 
 * @author KEYKB
 * @deprecated 请直接使用 {@link AssertUtils}
 */
@Deprecated
public class AssertUtil {

    /**
     * 断言表达式为真
     */
    public static void isTrue(boolean expression, String message) {
        AssertUtils.isTrue(expression, message);
    }

    /**
     * 断言表达式为假
     */
    public static void isFalse(boolean expression, String message) {
        AssertUtils.isFalse(expression, message);
    }

    /**
     * 断言对象非空
     */
    public static void notNull(Object obj, String message) {
        AssertUtils.notNull(obj, message);
    }

    /**
     * 断言对象为空
     */
    public static void isNull(Object obj, String message) {
        AssertUtils.isNull(obj, message);
    }

    /**
     * 断言字符串非空
     */
    public static void notEmpty(String str, String message) {
        AssertUtils.notEmpty(str, message);
    }

    /**
     * 断言集合非空
     */
    public static void notEmpty(java.util.Collection<?> collection, String message) {
        AssertUtils.notEmpty(collection, message);
    }

    /**
     * 断言Map非空
     */
    public static void notEmpty(java.util.Map<?, ?> map, String message) {
        AssertUtils.notEmpty(map, message);
    }
} 