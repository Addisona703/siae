package com.hngy.siae.attendance.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 考勤异常类型枚举
 *
 * @author SIAE Team
 */
@Getter
@AllArgsConstructor
public enum AnomalyType {

    /**
     * 迟到
     */
    LATE(0, "迟到"),

    /**
     * 早退
     */
    EARLY_DEPARTURE(1, "早退"),

    /**
     * 缺勤
     */
    ABSENCE(2, "缺勤"),

    /**
     * 漏签到
     */
    MISSING_CHECK_IN(3, "漏签到"),

    /**
     * 漏签退
     */
    MISSING_CHECK_OUT(4, "漏签退");

    @EnumValue
    @JsonValue
    private final Integer value;
    private final String description;
}
