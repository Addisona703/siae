package com.hngy.siae.auth.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 用户角色分配请求DTO
 * 
 * @author KEYKB
 */
@Data
public class UserRoleRequest {
    
    /**
     * 角色ID列表
     */
    @NotEmpty(message = "角色ID列表不能为空")
    private List<Long> roleIds;
} 