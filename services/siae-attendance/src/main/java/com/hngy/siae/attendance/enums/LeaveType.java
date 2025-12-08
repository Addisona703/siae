package com.hngy.siae.attendance.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.hngy.siae.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 请假类型枚举
 *
 * @author SIAE Team
 */
@Getter
@AllArgsConstructor
public enum LeaveType implements BaseEnum {

    /**
     * 病假
     */
    SICK_LEAVE(0, "病假"),

    /**
     * 事假
     */
    PERSONAL_LEAVE(1, "事假");

    @EnumValue
    private final Integer code;
    private final String description;

    @Override
    public int getCode() {
        return code;
    }
}
