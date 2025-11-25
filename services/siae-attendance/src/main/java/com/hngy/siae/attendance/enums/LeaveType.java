package com.hngy.siae.attendance.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 请假类型枚举
 *
 * @author SIAE Team
 */
@Getter
@AllArgsConstructor
public enum LeaveType {

    /**
     * 病假
     */
    SICK_LEAVE(0, "病假"),

    /**
     * 事假
     */
    PERSONAL_LEAVE(1, "事假");

    @EnumValue
    @JsonValue
    private final Integer value;
    private final String description;
}
