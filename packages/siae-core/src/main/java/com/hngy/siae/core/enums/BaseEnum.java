package com.hngy.siae.core.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 枚举基础接口
 * 所有业务枚举都应该实现此接口
 * 
 * JSON序列化规范：
 * - 序列化时统一输出 code（数字）
 * - 反序列化时支持 code 或 description
 * - 前端展示用的描述文字由前端自行映射，或通过额外字段返回
 * 
 * @author KEYKB
 */
public interface BaseEnum {
    /**
     * 获取枚举编码
     * 序列化时使用此值
     */
    @JsonValue
    int getCode();
    
    /**
     * 获取枚举描述
     */
    String getDescription();

    /**
     * 根据 code 获取枚举实例
     */
    static <T extends Enum<T> & BaseEnum> T fromCode(Class<T> clazz, int code) {
        for (T e : clazz.getEnumConstants()) {
            if (e.getCode() == code) {
                return e;
            }
        }
        return null;
    }

    /**
     * 根据描述获取枚举实例
     */
    static <T extends Enum<T> & BaseEnum> T fromDesc(Class<T> clazz, String desc) {
        for (T e : clazz.getEnumConstants()) {
            if (e.getDescription().equals(desc)) {
                return e;
            }
        }
        return null;
    }
}
