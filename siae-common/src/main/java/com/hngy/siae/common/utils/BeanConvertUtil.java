package com.hngy.siae.common.utils;

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
                .map(source -> to(source, targetClass))
                .collect(Collectors.toList());
    }

}
