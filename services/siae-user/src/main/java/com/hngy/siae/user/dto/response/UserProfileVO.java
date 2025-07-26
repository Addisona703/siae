package com.hngy.siae.user.dto.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 用户详情视图对象
 *
 * @author KEYKB
 */
@Data
public class UserProfileVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 主页背景图片URL
     */
    private String bgUrl;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * QQ号
     */
    private String qq;

    /**
     * 微信号
     */
    private String wechat;

    /**
     * 性别：0未知，1男，2女
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDate birthday;
}
