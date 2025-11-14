package com.hngy.siae.content.enums.status;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.hngy.siae.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 分类状态枚举类
 */
@AllArgsConstructor
@Getter
public enum CategoryStatusEnum implements BaseEnum {
    //
    DISABLED(0, "禁用"),
    ENABLED(1, "启用"),
    DELETED(2, "已删除");

    @EnumValue
    private final int code;
    private final String description;
}
