package com.hngy.siae.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.hngy.siae.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 成员生命周期状态枚举
 * 用于 Membership 表的 lifecycleStatus 字段
 *
 * @author KEYKB
 */
@Getter
@AllArgsConstructor
public enum LifecycleStatusEnum implements BaseEnum {

    /**
     * 候选成员
     */
    CANDIDATE(0, "候选成员"),

    /**
     * 正式成员
     */
    OFFICIAL(1, "正式成员");

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
     * 判断是否为候选成员
     *
     * @param code 状态码
     * @return true 如果是候选成员
     */
    public static boolean isCandidate(int code) {
        return CANDIDATE.getCode() == code;
    }

    /**
     * 判断是否为正式成员
     *
     * @param code 状态码
     * @return true 如果是正式成员
     */
    public static boolean isOfficial(int code) {
        return OFFICIAL.getCode() == code;
    }
}
