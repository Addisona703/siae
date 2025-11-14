package com.hngy.siae.content.enums.status;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.hngy.siae.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 问题状态枚举类
 */
@AllArgsConstructor
@Getter
public enum QuestionStatusEnum implements BaseEnum {
    //
    UNSOLVED(0, "未解决"),
    SOLVED(1, "已解决");

    @EnumValue
    private final int code;
    private final String description;
}
