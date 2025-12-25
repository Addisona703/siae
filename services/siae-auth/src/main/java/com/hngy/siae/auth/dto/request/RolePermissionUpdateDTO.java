package com.hngy.siae.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
     * 权限ID列表（可以为空列表，表示清空所有权限）
     */
    @NotNull(message = "权限ID列表不能为null")
    @Schema(description = "权限ID列表（可以为空列表，表示清空所有权限）", example = "[1, 2, 3]", required = true)
    private List<Long> permissionIds;
}
