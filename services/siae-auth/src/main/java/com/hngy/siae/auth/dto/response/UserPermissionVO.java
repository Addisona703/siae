package com.hngy.siae.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户权限关联VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionVO {
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 权限ID
     */
    private Long permissionId;
    
    /**
     * 权限名称
     */
    private String permissionName;
    
    /**
     * 权限编码
     */
    private String permissionCode;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
} 