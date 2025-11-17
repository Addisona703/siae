package com.hngy.siae.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 第三方账号绑定请求DTO
 * 
 * @author SIAE
 */
@Data
public class AccountBindDTO {
    
    /**
     * 提供商: github/google/wechat/qq
     */
    @NotBlank(message = "提供商不能为空")
    private String provider;
    
    /**
     * 授权码
     */
    @NotBlank(message = "授权码不能为空")
    private String code;
    
    /**
     * 状态参数
     */
    private String state;
}
