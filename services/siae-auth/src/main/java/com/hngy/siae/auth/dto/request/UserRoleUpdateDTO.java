package com.hngy.siae.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户角色更新DTO
 * 
 * @author KEYKB
 */
@Data
@Schema(description = "用户角色更新请求")
public class UserRoleUpdateDTO {
    
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1")
    private Long userId;
    
    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    @Schema(description = "角色ID", example = "2")
    private Long roleId;
}
