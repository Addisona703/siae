package com.hngy.siae.attendance.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.hngy.siae.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 规则适用对象类型枚举
 *
 * @author SIAE Team
 */
@Getter
@AllArgsConstructor
public enum RuleTargetType implements BaseEnum {

    /**
     * 全体成员
     */
    ALL(0, "全体成员"),

    /**
     * 部门
     */
    DEPARTMENT(1, "部门"),

    /**
     * 个人
     */
    INDIVIDUAL(2, "个人");

    @EnumValue
    private final Integer code;
    private final String description;

    @Override
    public int getCode() {
        return code;
    }
}
