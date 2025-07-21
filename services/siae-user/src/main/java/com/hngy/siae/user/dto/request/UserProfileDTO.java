package com.hngy.siae.user.dto.request;

import com.hngy.siae.common.validation.CreateGroup;
import com.hngy.siae.common.validation.UpdateGroup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 用户详情数据传输对象
 *
 * @author KEYKB
 */
@Data
public class UserProfileDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Long userId;

    /**
     * 昵称
     */
    @Size(max = 64, message = "昵称长度不能超过64个字符", groups = {CreateGroup.class})
    private String nickname;

    /**
     * 真实姓名
     */
    @Size(max = 64, message = "真实姓名长度不能超过64个字符", groups = {CreateGroup.class})
    private String realName;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确", groups = {CreateGroup.class})
    private String email;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确", groups = {CreateGroup.class})
    private String phone;

    /**
     * QQ号
     */
    @Pattern(regexp = "^[1-9][0-9]{4,10}$", message = "QQ号格式不正确", groups = {CreateGroup.class})
    private String qq;

    /**
     * 微信号
     */
    @Size(max = 64, message = "微信号长度不能超过64个字符", groups = {CreateGroup.class})
    private String wechat;

    /**
     * 身份证号
     */
    @Pattern(regexp = "(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)", message = "身份证号格式不正确", groups = {CreateGroup.class})
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