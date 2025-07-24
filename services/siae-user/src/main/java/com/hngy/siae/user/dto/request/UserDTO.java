package com.hngy.siae.user.dto.request;

import com.hngy.siae.core.validation.CreateGroup;
import com.hngy.siae.core.validation.LoginGroup;
import com.hngy.siae.core.validation.UpdateGroup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户数据传输对象
 * 
 * @author KEYKB
 */
@Data
public class UserDTO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空", groups = {UpdateGroup.class})
    private Long id;
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空", groups = {CreateGroup.class})
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间", groups = {CreateGroup.class, UpdateGroup.class})
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线", groups = {CreateGroup.class, UpdateGroup.class})
    private String username;
    
//    /**
//     * 登录账号（用户名/邮箱/手机号）
//     */
//    @NotBlank(message = "登录账号不能为空", groups = {LoginGroup.class})
//    private String account;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空", groups = {CreateGroup.class, LoginGroup.class})
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间", groups = {CreateGroup.class, UpdateGroup.class})
    private String password;
    
//    /**
//     * 确认密码
//     */
//    @NotBlank(message = "确认密码不能为空", groups = {CreateGroup.class})
//    private String confirmPassword;
    
    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确", groups = {CreateGroup.class, UpdateGroup.class})
    private String email;
    
    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确", groups = {CreateGroup.class, UpdateGroup.class})
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
     * 班级ID，关联ClassInfo实体
     */
    private Long classId;
    
    /**
     * 验证码
     */
    private String captcha;
    
    /**
     * 记住我
     */
    private Boolean rememberMe = false;
    
    /**
     * 登录IP
     */
    private String loginIp;
    
    /**
     * 状态：0禁用，1启用
     */
    private Integer status;
} 