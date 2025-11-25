package com.hngy.siae.core.asserts;

import com.hngy.siae.core.exception.ServiceException;
import com.hngy.siae.core.result.IResultCode;
import cn.hutool.core.util.StrUtil;

import java.util.Collection;
import java.util.Map;

/**
 * 断言工具类：用于简化业务逻辑校验，统一抛出业务异常。
 * 推荐在 com.hngy.siae.content.service 层做参数检查、状态校验、前置判断等。
 */
public class AssertUtils {

    public static void fail(IResultCode resultCode) {
        throw new ServiceException(resultCode);
    }

    public static void fail(String message) {
        throw new ServiceException(message);
    }

    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new ServiceException(message);
        }
    }

    public static void isFalse(boolean expression, String message) {
        if (expression) {
            throw new ServiceException(message);
        }
    }

    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new ServiceException(message);
        }
    }

    public static void isNull(Object obj, String message) {
        if (obj != null) {
            throw new ServiceException(message);
        }
    }

    public static void notEmpty(String str, String message) {
        if (StrUtil.isBlank(str)) {
            throw new ServiceException(message);
        }
    }

    public static void notEmpty(Collection<?> collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new ServiceException(message);
        }
    }

    public static void notEmpty(Map<?, ?> map, String message) {
        if (map == null || map.isEmpty()) {
            throw new ServiceException(message);
        }
    }

    // --- 新增的、推荐使用的方法，附带了错误码 ---
    public static void isTrue(boolean expression, IResultCode resultCode) {
        if (!expression) {
            throw new ServiceException(resultCode);
        }
    }

    public static void isFalse(boolean expression, IResultCode resultCode) {
        if (expression) {
            throw new ServiceException(resultCode);
        }
    }

    public static void notNull(Object object, IResultCode resultCode) {
        if (object == null) {
            throw new ServiceException(resultCode);
        }
    }

    public static void isNull(Object object, IResultCode resultCode) {
        if (object != null) {
            throw new ServiceException(resultCode);
        }
    }

    public static void notEmpty(String str, IResultCode resultCode) {
        if (StrUtil.isBlank(str)) {
            throw new ServiceException(resultCode);
        }
    }

    public static void notEmpty(Collection<?> collection, IResultCode resultCode) {
        if (collection == null || collection.isEmpty()) {
            throw new ServiceException(resultCode);
        }
    }

    public static void notEmpty(Map<?, ?> map, IResultCode resultCode) {
        if (map == null || map.isEmpty()) {
            throw new ServiceException(resultCode);
        }
    }

    /**
     * 工具类构造函数断言，防止工具类被实例化
     *
     * @throws UnsupportedOperationException 总是抛出此异常
     */
    public static void utilityClass() {
        throw new UnsupportedOperationException("这是一个工具类，不能被实例化");
    }
}
