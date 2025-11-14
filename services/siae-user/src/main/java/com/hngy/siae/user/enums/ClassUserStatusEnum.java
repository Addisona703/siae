package com.hngy.siae.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.hngy.siae.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 班级用户状态枚举
 * 用于 ClassUser (major_class_enrollment) 表的 status 字段
 * 简化版：只区分在读和离校
 *
 * @author KEYKB
 */
@Getter
@AllArgsConstructor
public enum ClassUserStatusEnum implements BaseEnum {

    /**
     * 在读
     */
    ENROLLED(1, "在读"),

    /**
     * 离校（包括毕业、退学等）
     */
    LEFT(2, "离校");

    /**
     * 状态码
     */
    @EnumValue
    private final int code;

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 判断是否在读
     *
     * @param code 状态码
     * @return true 如果在读
     */
    public static boolean isEnrolled(int code) {
        return ENROLLED.getCode() == code;
    }

    /**
     * 判断是否已离校
     *
     * @param code 状态码
     * @return true 如果已离校
     */
    public static boolean hasLeft(int code) {
        return LEFT.getCode() == code;
    }
}
