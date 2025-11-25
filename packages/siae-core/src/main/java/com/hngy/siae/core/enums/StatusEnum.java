package com.hngy.siae.core.enums;

import lombok.Getter;

/**
 * 通用状态枚举
 * 
 * @author SIAE开发团队
 */
@Getter
public enum StatusEnum {
    
    /**
     * 启用状态
     */
    ENABLED(1, "启用"),
    
    /**
     * 禁用状态
     */
    DISABLED(0, "禁用");

    private final Integer code;
    private final String description;

    StatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     * 
     * @param code 状态代码
     * @return 状态枚举
     */
    public static StatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (StatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否为启用状态
     * 
     * @param code 状态代码
     * @return 是否启用
     */
    public static boolean isEnabled(Integer code) {
        return ENABLED.code.equals(code);
    }
}
