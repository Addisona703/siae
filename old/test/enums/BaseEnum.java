package com.hngy.siae.common.enums;

public interface BaseEnum {
    int getCode();
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
