package com.hngy.siae.content.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TypeEnum implements BaseEnum {
    // 内容类型枚举
    CONTENT(0, "content"),
    COMMENT(1, "comment");

    @EnumValue
    private final int code;
    private final String description;
}
