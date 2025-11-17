package com.hngy.siae.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 第三方账号解绑请求DTO
 * 
 * @author SIAE
 */
@Data
public class AccountUnbindDTO {
    
    /**
     * 提供商: github/google/wechat/qq
     */
    @NotBlank(message = "提供商不能为空")
    private String provider;
}
