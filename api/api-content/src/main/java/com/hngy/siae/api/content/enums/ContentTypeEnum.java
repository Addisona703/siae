package com.hngy.siae.api.content.enums;

import com.hngy.siae.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * 内容类型枚举类
 *
 * @author KEYKB
 */
@AllArgsConstructor
@Getter
public enum ContentTypeEnum implements BaseEnum, Serializable {
    // 内容类型枚举
    ARTICLE(0, "article"),
    NOTE(1, "note"),
    QUESTION(2, "question"),
    FILE(3, "file"),
    VIDEO(4, "video");

    private final int code;
    private final String description;
}
