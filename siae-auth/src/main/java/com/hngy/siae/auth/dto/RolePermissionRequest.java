package com.hngy.siae.auth.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 角色权限分配请求DTO
 * 
 * @author KEYKB
 */
@Data
public class RolePermissionRequest {
    
    /**
     * 权限ID列表
     */
    @NotEmpty(message = "权限ID列表不能为空")
    private List<Long> permissionIds;
} 