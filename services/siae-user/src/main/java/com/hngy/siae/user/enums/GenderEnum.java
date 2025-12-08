package com.hngy.siae.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.hngy.siae.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 性别枚举
 * 用于 UserProfile 表的 gender 字段
 *
 * @author KEYKB
 */
@Getter
@AllArgsConstructor
public enum GenderEnum implements BaseEnum {

    /**
     * 未知
     */
    UNKNOWN(0, "未知"),

    /**
     * 男
     */
    MALE(1, "男"),

    /**
     * 女
     */
    FEMALE(2, "女");

    /**
     * 性别码
     */
    @EnumValue
    private final int code;

    /**
     * 性别描述
     */
    private final String description;
}
