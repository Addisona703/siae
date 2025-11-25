package com.hngy.siae.core.enums;

/**
 * 枚举基础接口
 * 所有业务枚举都应该实现此接口
 * 
 * @author KEYKB
 */
public interface BaseEnum {
    /**
     * 获取枚举编码
     */
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
