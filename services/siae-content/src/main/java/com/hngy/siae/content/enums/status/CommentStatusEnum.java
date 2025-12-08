package com.hngy.siae.content.enums.status;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.hngy.siae.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 评论状态枚举类（与内容状态保持一致）
 * 0=草稿, 1=待审核, 2=已发布, 3=已删除
 */
@AllArgsConstructor
@Getter
public enum CommentStatusEnum implements BaseEnum {
    //
    DRAFT(0, "草稿"),
    PENDING(1, "待审核"),
    PUBLISHED(2, "已发布"),
    DELETED(3, "已删除");

    @EnumValue
    private final int code;
    private final String description;
}
