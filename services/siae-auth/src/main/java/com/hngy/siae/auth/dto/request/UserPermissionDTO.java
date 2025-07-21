package com.hngy.siae.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户权限关联DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionDTO {
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    /**
     * 权限ID列表
     */
    @NotNull(message = "权限ID列表不能为空")
    private List<Long> permissionIds;
} 