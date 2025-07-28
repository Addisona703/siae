package com.hngy.siae.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户角色关联响应VO
 * 
 * @author KEYKB
 */
@Data
@Schema(description = "用户角色关联信息")
public class UserRoleVO {
    
    /**
     * 用户角色关联ID
     */
    @Schema(description = "用户角色关联ID", example = "1")
    private Long id;
    
    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long userId;
    
    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "admin")
    private String username;
    
    /**
     * 角色ID
     */
    @Schema(description = "角色ID", example = "2")
    private Long roleId;
    
    /**
     * 角色名称
     */
    @Schema(description = "角色名称", example = "管理员")
    private String roleName;
    
    /**
     * 角色编码
     */
    @Schema(description = "角色编码", example = "ROLE_ADMIN")
    private String roleCode;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    private LocalDateTime createdAt;
}
