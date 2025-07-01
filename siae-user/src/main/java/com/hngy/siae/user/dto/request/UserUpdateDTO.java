package com.hngy.siae.user.dto.request;

import com.hngy.siae.common.validation.UpdateGroup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户更新数据传输对象
 * 
 * @author KEYKB
 */
@Data
public class UserUpdateDTO implements Serializable {
    
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
    @Size(min = 4, max = 20, message = "用户名长度必须在4-20个字符之间", groups = {UpdateGroup.class})
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线", groups = {UpdateGroup.class})
    private String username;
    
    /**
     * 密码
     */
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间", groups = {UpdateGroup.class})
    private String password;
    
    /**
     * 状态：0禁用，1启用
     */
    private Integer status;
    
    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确", groups = {UpdateGroup.class})
    private String email;
    
    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确", groups = {UpdateGroup.class})
    private String phone;
} 