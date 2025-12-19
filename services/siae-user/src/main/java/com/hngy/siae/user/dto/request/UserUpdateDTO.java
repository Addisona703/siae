package com.hngy.siae.user.dto.request;

import com.hngy.siae.user.enums.UserStatusEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户更新数据传输对象
 * <p>
 * 用于用户更新操作的数据传输。
 * ID 字段可选，由 Controller 从路径参数设置。
 *
 * @author KEYKB
 */
@Data
public class UserUpdateDTO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID（可选，由Controller从路径参数设置）
     */
    private Long id;

    /**
     * 用户名
     */
    @Size(min = 4, max = 20, message = "用户名长度必须在4-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    /**
     * 密码
     */
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    /**
     * 学号
     */
    @Size(max = 32, message = "学号长度不能超过32个字符")
    private String studentId;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 头像文件ID
     */
    private String avatarFileId;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 专业ID
     */
    private Long majorId;

    /**
     * 入学年份
     */
    private Integer entryYear;

    /**
     * 班号
     */
    private Integer classNo;

    /**
     * 状态：0禁用，1启用
     */
    private UserStatusEnum status;

    /**
     * QQ号
     */
    @Pattern(regexp = "^[1-9]\\d{4,10}$", message = "QQ号格式不正确")
    private String qq;

    /**
     * 身份证号
     */
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$", message = "身份证号格式不正确")
    private String idCard;
} 
