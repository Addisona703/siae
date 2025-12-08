package com.hngy.siae.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.hngy.siae.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户状态枚举
 * 用于 User 表的 status 字段
 *
 * @author KEYKB
 */
@Getter
@AllArgsConstructor
public enum UserStatusEnum implements BaseEnum {

    /**
     * 禁用
     */
    DISABLED(0, "禁用"),

    /**
     * 启用
     */
    ENABLED(1, "启用");

    /**
     * 状态码
     */
    @EnumValue
    private final int code;

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 判断是否启用
     *
     * @param code 状态码
     * @return true 如果启用
     */
    public static boolean isEnabled(int code) {
        return ENABLED.getCode() == code;
    }
}
