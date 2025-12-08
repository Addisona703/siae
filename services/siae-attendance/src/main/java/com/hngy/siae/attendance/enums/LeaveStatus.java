package com.hngy.siae.attendance.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.hngy.siae.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 请假状态枚举
 *
 * @author SIAE Team
 */
@Getter
@AllArgsConstructor
public enum LeaveStatus implements BaseEnum {

    /**
     * 待审核
     */
    PENDING(0, "待审核"),

    /**
     * 已批准
     */
    APPROVED(1, "已批准"),

    /**
     * 已拒绝
     */
    REJECTED(2, "已拒绝"),

    /**
     * 已撤销
     */
    CANCELLED(3, "已撤销");

    @EnumValue
    private final Integer code;
    private final String description;

    @Override
    public int getCode() {
        return code;
    }
}
