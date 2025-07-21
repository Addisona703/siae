package com.hngy.siae.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新令牌请求DTO
 * 
 * @author KEYKB
 */
@Data
public class TokenRefreshDTO {
    
    /**
     * 刷新令牌
     */
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
} 