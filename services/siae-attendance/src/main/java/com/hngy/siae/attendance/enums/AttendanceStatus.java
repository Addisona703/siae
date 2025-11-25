package com.hngy.siae.attendance.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 考勤状态枚举
 *
 * @author SIAE Team
 */
@Getter
@AllArgsConstructor
public enum AttendanceStatus {

    /**
     * 进行中
     */
    IN_PROGRESS(0, "进行中"),

    /**
     * 已完成
     */
    COMPLETED(1, "已完成"),

    /**
     * 异常
     */
    ABNORMAL(2, "异常");

    @EnumValue
    @JsonValue
    private final Integer value;
    private final String description;
}
