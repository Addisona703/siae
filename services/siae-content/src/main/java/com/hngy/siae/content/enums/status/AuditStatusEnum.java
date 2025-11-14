package com.hngy.siae.content.enums.status;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.hngy.siae.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审核状态枚举类
 */
@AllArgsConstructor
@Getter
public enum AuditStatusEnum implements BaseEnum {
    //
    PENDING(0, "待审核"),
    APPROVED(1, "通过"),
    DELETED(2, "已删除");

    @EnumValue
    private final int code;
    private final String description;
}
