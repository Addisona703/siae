package com.hngy.siae.content.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ActionTypeEnum implements BaseEnum {
    //
    VIEW(0, "view"),
    LIKE(1, "like"),
    FAVORITE(2, "favorite");

    @EnumValue
    private final int code;
    private final String description;
}
