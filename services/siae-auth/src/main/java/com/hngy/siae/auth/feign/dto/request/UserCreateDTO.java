package com.hngy.siae.auth.feign.dto.request;

import com.hngy.siae.core.validation.CreateGroup;
import com.hngy.siae.core.validation.LoginGroup;
import com.hngy.siae.core.validation.UpdateGroup;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 用户创建数据传输对象
 * <p>
 * 用于用户一体化创建操作的数据传输，包含用户基本信息、用户详情信息和班级关联信息。
 * 支持三阶段创建流程：用户基本信息 → 用户详情信息 → 班级关联信息（可选）。
 * 不包含ID字段，因为创建时ID由系统自动生成。
 *
 * @author KEYKB
 */
@Data
public class UserCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ==================== 用户基本信息字段（存储到 user 表） ====================

    /**
     * 用户名（必填）
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    /**
     * 密码（必填）
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    /**
     * 状态：1启用，0禁用（默认值1）
     */
    private Integer status = 1;

    // ==================== 用户详情字段（存储到 user_profile 表） ====================

    /**
     * 真实姓名
     */
    @Size(max = 64, message = "真实姓名长度不能超过64个字符")
    private String realName;

    /**
     * 昵称
     */
    @Size(max = 64, message = "昵称长度不能超过64个字符")
    private String nickname;

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
     * 用户头像URL
     */
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatar;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 性别：0未知，1男，2女
     */
    @Min(value = 0, message = "性别值不能小于0")
    @Max(value = 2, message = "性别值不能大于2")
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDate birthday;

    /**
     * 身份证号
     */
    @Pattern(regexp = "(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)", message = "身份证号格式不正确")
    private String idCard;

    /**
     * QQ号
     */
    @Pattern(regexp = "^[1-9][0-9]{4,10}$", message = "QQ号格式不正确")
    private String qq;

    /**
     * 微信号
     */
    @Size(max = 64, message = "微信号长度不能超过64个字符")
    private String wechat;

    /**
     * 主页背景图片URL
     */
    @Size(max = 512, message = "背景图片URL长度不能超过512个字符")
    private String bgUrl;

    // ==================== 班级关联字段（可选，存储到 class_user 表） ====================

    /**
     * 班级ID（可选）
     */
    private Long classId;

    /**
     * 成员类型：0非协会成员，1预备成员，2正式成员（默认值0）
     */
    @Min(value = 0, message = "成员类型值不能小于0")
    @Max(value = 2, message = "成员类型值不能大于2")
    private Integer memberType = 0;

    /**
     * 入会日期（可选）
     */
    private LocalDate joinDate;
}
