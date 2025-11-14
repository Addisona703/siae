package com.hngy.siae.content.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.hngy.siae.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 内容类型枚举类
 */
@AllArgsConstructor
@Getter
public enum ContentTypeEnum implements BaseEnum {
    // 内容类型枚举
    ARTICLE(0, "article"),
    NOTE(1, "note"),
    QUESTION(2, "question"),
    FILE(3, "file"),
    VIDEO(4, "video");

    @EnumValue
    private final int code;
    private final String description;
}

