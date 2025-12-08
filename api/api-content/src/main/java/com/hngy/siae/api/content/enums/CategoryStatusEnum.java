package com.hngy.siae.api.content.enums;

import com.hngy.siae.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * 分类状态枚举类
 *
 * @author KEYKB
 */
@AllArgsConstructor
@Getter
public enum CategoryStatusEnum implements BaseEnum, Serializable {
    //
    DISABLED(0, "禁用"),
    ENABLED(1, "启用"),
    DELETED(2, "已删除");

    private final int code;
    private final String description;
}
