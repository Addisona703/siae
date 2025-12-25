package com.hngy.siae.auth.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 角色权限分配请求DTO
 * 
 * @author KEYKB
 */
@Data
public class RolePermissionDTO {
    
    /**
     * 权限ID列表（追加权限时不能为空）
     */
    @NotEmpty(message = "权限ID列表不能为空")
    private List<Long> permissionIds;
} 