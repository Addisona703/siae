package com.hngy.siae.attendance.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.hngy.siae.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 考勤状态枚举
 *
 * @author SIAE Team
 */
@Getter
@AllArgsConstructor
public enum AttendanceStatus implements BaseEnum {

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
    private final Integer code;
    private final String description;

    @Override
    public int getCode() {
        return code;
    }
}
