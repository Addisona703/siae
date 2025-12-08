package com.hngy.siae.api.user.enums;

import com.hngy.siae.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 班级成员类型枚举
 * 用于 ClassUser (major_class_enrollment) 表的 memberType 字段
 * 简化版：只区分是否为协会成员
 *
 * @author KEYKB
 */
@Getter
@AllArgsConstructor
public enum MemberTypeEnum implements BaseEnum {

    /**
     * 非协会成员
     */
    NON_MEMBER(0, "非协会成员"),

    /**
     * 协会成员（包括候选和正式）
     */
    MEMBER(1, "协会成员");

    /**
     * 类型码
     */
    private final int code;

    /**
     * 类型描述
     */
    private final String description;

    /**
     * 判断是否为协会成员
     *
     * @param code 类型码
     * @return true 如果是协会成员
     */
    public static boolean isMember(int code) {
        return MEMBER.getCode() == code;
    }
}
