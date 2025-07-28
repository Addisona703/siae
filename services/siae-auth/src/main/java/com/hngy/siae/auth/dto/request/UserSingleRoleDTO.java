package com.hngy.siae.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户单角色分配请求DTO
 * 
 * @author KEYKB
 */
@Data
@Schema(description = "用户单角色分配请求")
public class UserSingleRoleDTO {
    
    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    @Schema(description = "角色ID", example = "1")
    private Long roleId;
}
