package com.hngy.siae.attendance.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 考勤类型枚举
 *
 * @author SIAE Team
 */
@Getter
@AllArgsConstructor
public enum AttendanceType {

    /**
     * 日常考勤
     */
    DAILY(0, "日常考勤"),

    /**
     * 活动考勤
     */
    ACTIVITY(1, "活动考勤");

    @EnumValue
    @JsonValue
    private final Integer value;
    private final String description;

    /**
     * 根据值获取枚举
     *
     * @param value 值
     * @return 枚举
     */
    public static AttendanceType fromValue(Integer value) {
        if (value == null) {
            return DAILY;
        }
        for (AttendanceType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return DAILY;
    }
}
