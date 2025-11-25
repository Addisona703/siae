package com.hngy.siae.attendance.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 规则状态枚举
 *
 * @author SIAE Team
 */
@Getter
@AllArgsConstructor
public enum RuleStatus {

    /**
     * 禁用
     */
    DISABLED(0, "禁用"),

    /**
     * 启用
     */
    ENABLED(1, "启用");

    @EnumValue
    @JsonValue
    private final Integer value;
    private final String description;
}
