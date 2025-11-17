package com.hngy.siae.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 第三方账号解绑请求DTO
 * 
 * @author SIAE
 */
@Data
public class UnbindRequest {
    
    /**
     * 提供商: qq/wx/github
     */
    @NotBlank(message = "提供商不能为空")
    private String provider;
}
