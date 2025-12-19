package com.hngy.siae.core.utils;

import cn.hutool.core.bean.BeanUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Bean 属性转换工具类
 * 支持 Entity ↔ DTO ↔ VO 等对象之间的属性复制
 *
 * @author KEYKB
 */
public class BeanConvertUtil {

    /**
     * 通用 Entity → VO 转换
     * @param source 原始对象
     * @param targetClass 目标类
     * @return 转换后的对象
     */
    public static <S, T> T to(S source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        return BeanUtil.copyProperties(source, targetClass);
    }

    /**
     * 通用 Entity → VO 转换，支持忽略特定字段
     * @param source 原始对象
     * @param targetClass 目标类
     * @param ignoreProperties 需要忽略的字段
     * @return 转换后的对象
     */
    public static <S, T> T to(S source, Class<T> targetClass, String... ignoreProperties) {
        if (source == null) {
            return null;
        }
        return BeanUtil.copyProperties(source, targetClass, ignoreProperties);
    }

    /**
     * 将源对象属性复制到目标对象，支持排除特定字段
     *
     * @param source 源对象
     * @param target 目标对象
     * @param ignoreProperties 需要忽略的字段
     */
    public static void to(Object source, Object target, String... ignoreProperties) {
        if (source == null || target == null) {
            return;
        }
        BeanUtil.copyProperties(source, target, ignoreProperties);
    }

    /**
     * 将源对象属性复制到目标对象，支持排除特定字段和控制是否忽略null值
     *
     * @param source 源对象
     * @param target 目标对象
     * @param ignoreNullValue 是否忽略null值（true=忽略null值，false=复制null值）
     * @param ignoreProperties 需要忽略的字段
     */
    public static void to(Object source, Object target, boolean ignoreNullValue, String... ignoreProperties) {
        if (source == null || target == null) {
            return;
        }
        BeanUtil.copyProperties(source, target, 
            cn.hutool.core.bean.copier.CopyOptions.create()
                .setIgnoreNullValue(ignoreNullValue)
                .setIgnoreProperties(ignoreProperties));
    }

    /**
     * 批量转换 Entity → VO
     * @param sourceList 原始对象列表
     * @param targetClass 目标类
     * @return 转换后的 VO 列表
     */
    public static <S, T> List<T> toList(List<S> sourceList, Class<T> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            return List.of();
        }
        return sourceList.stream()
                .map(source -> BeanUtil.copyProperties(source, targetClass))
                .collect(Collectors.toList());
    }

}
