package com.hngy.siae.content.common.enums.status;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.hngy.siae.content.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作状态枚举类
 */
@AllArgsConstructor
@Getter
public enum ActionStatusEnum implements BaseEnum {
    //
    CANCELLED(0, "取消"),
    ACTIVATED(1, "激活");

    @EnumValue
    private final int code;
    private final String description;
}
