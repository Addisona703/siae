package com.hngy.siae.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 用户详情更新数据传输对象
 * <p>
 * 用于用户详情更新操作的数据传输，包含用户ID字段和可更新的字段。
 * 用户ID字段必须提供，用于标识要更新的记录。
 *
 * @author KEYKB
 */
@Data
public class UserProfileUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID（作为主键）
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 昵称
     */
    @Size(max = 64, message = "昵称长度不能超过64个字符")
    private String nickname;

    /**
     * 真实姓名
     */
    @Size(max = 64, message = "真实姓名长度不能超过64个字符")
    private String realName;

    /**
     * 用户头像URL
     */
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatar;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 主页背景图片URL
     */
    @Size(max = 512, message = "背景图片URL长度不能超过512个字符")
    private String bgUrl;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱长度不能超过128个字符")
    private String email;

    /**
     * 手机号码
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * QQ号 (用于联系)
     */
    @Pattern(regexp = "^[1-9][0-9]{4,10}$", message = "QQ号格式不正确")
    private String qq;

    /**
     * 微信号 (用于联系)
     */
    @Size(max = 64, message = "微信号长度不能超过64个字符")
    private String wechat;

    /**
     * 身份证号
     */
    @Pattern(regexp = "(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)", message = "身份证号格式不正确")
    private String idCard;

    /**
     * 性别：0未知，1男，2女
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDate birthday;
}
