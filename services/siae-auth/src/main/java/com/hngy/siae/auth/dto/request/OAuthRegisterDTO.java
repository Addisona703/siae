package com.hngy.siae.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * OAuth用户完善信息请求DTO
 * 
 * @author SIAE
 */
@Data
public class OAuthRegisterDTO {
    
    /**
     * 临时令牌（从OAuth回调获取）
     */
    @NotBlank(message = "临时令牌不能为空")
    private String tempToken;
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    private String username;
    
    /**
     * 邮箱（可选）
     */
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /**
     * 密码（可选，用于后续账号密码登录）
     */
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;
}
