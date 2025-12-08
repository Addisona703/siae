package com.hngy.siae.attendance.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.hngy.siae.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 考勤类型枚举
 *
 * @author SIAE Team
 */
@Getter
@AllArgsConstructor
public enum AttendanceType implements BaseEnum {

    /**
     * 日常考勤
     */
    DAILY(0, "日常考勤"),

    /**
     * 活动考勤
     */
    ACTIVITY(1, "活动考勤");

    @EnumValue
    private final Integer code;
    private final String description;

    @Override
    public int getCode() {
        return code;
    }

    /**
     * 根据值获取枚举
     *
     * @param value 值
     * @return 枚举
     */
    public static AttendanceType fromValue(Integer value) {
        return BaseEnum.fromCode(AttendanceType.class, value);
    }
}
