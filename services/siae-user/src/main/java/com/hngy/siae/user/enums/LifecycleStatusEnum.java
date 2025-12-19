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
     * 待审核（申请入会）
     */
    PENDING(0, "待审核"),

    /**
     * 候选成员（审核通过）
     */
    CANDIDATE(1, "候选成员"),

    /**
     * 正式成员（转正）
     */
    OFFICIAL(2, "正式成员"),

    /**
     * 已拒绝
     */
    REJECTED(3, "已拒绝"),

    /**
     * 已开除（强制退会）
     */
    EXPELLED(4, "已开除");

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
     * 判断是否为待审核
     *
     * @param code 状态码
     * @return true 如果是待审核
     */
    public static boolean isPending(int code) {
        return PENDING.getCode() == code;
    }

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

    /**
     * 判断是否已拒绝
     *
     * @param code 状态码
     * @return true 如果已拒绝
     */
    public static boolean isRejected(int code) {
        return REJECTED.getCode() == code;
    }

    /**
     * 判断是否已开除
     *
     * @param code 状态码
     * @return true 如果已开除
     */
    public static boolean isExpelled(int code) {
        return EXPELLED.getCode() == code;
    }
}
