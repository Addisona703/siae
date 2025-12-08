package com.hngy.siae.attendance.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.hngy.siae.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 规则状态枚举
 *
 * @author SIAE Team
 */
@Getter
@AllArgsConstructor
public enum RuleStatus implements BaseEnum {

    /**
     * 禁用
     */
    DISABLED(0, "禁用"),

    /**
     * 启用
     */
    ENABLED(1, "启用");

    @EnumValue
    private final Integer code;
    private final String description;

    @Override
    public int getCode() {
        return code;
    }
}
