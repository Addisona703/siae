package com.hngy.siae.attendance.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 规则适用对象类型枚举
 *
 * @author SIAE Team
 */
@Getter
@AllArgsConstructor
public enum RuleTargetType {

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
    @JsonValue
    private final Integer value;
    private final String description;
}
