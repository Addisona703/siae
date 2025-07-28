package com.hngy.siae.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 角色权限更新请求DTO
 * 
 * @author SIAE开发团队
 */
@Data
@Schema(description = "角色权限更新请求参数")
public class RolePermissionUpdateDTO {
    
    /**
     * 权限ID列表
     */
    @NotEmpty(message = "权限ID列表不能为空")
    @Schema(description = "权限ID列表", example = "[1, 2, 3]", required = true)
    private List<Long> permissionIds;
}
