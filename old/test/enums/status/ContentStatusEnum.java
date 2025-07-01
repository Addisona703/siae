package com.hngy.siae.common.enums.status;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.hngy.siae.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * 内容状态枚举
 *
 * @author KEYKB
 * &#064;date: 2025/05/19
 */

@AllArgsConstructor
@Getter
public enum ContentStatusEnum implements BaseEnum {
    //
    DRAFT(0, "草稿"),
    PENDING(1, "待审核"),
    PUBLISHED(2, "已发布"),
    TRASH(3, "垃圾箱"),
    DELETED(4, "已删除");

    @EnumValue
    private final int code;
    private final String description;
}
