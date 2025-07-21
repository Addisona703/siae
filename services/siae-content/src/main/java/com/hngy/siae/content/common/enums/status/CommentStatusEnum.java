package com.hngy.siae.content.common.enums.status;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.hngy.siae.content.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 评论状态枚举类
 */
@AllArgsConstructor
@Getter
public enum CommentStatusEnum implements BaseEnum {
    //
    PENDING(0, "待审核"),
    APPROVED(1, "通过"),
    DELETED(2, "已删除");

    @EnumValue
    private final int code;
    private final String description;
}
