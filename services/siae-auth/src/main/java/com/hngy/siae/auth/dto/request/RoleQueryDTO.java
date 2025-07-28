package com.hngy.siae.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色查询请求DTO
 * 
 * @author SIAE开发团队
 */
@Data
@Schema(description = "角色查询请求参数")
public class RoleQueryDTO {
    
    /**
     * 角色名称（模糊查询）
     */
    @Schema(description = "角色名称，支持模糊查询", example = "管理员")
    private String name;
    
    /**
     * 角色编码（模糊查询）
     */
    @Schema(description = "角色编码，支持模糊查询", example = "admin")
    private String code;
    
    /**
     * 状态：0禁用，1启用
     */
    @Schema(description = "角色状态：0禁用，1启用", example = "1")
    private Integer status;
    
    /**
     * 创建时间范围 - 开始时间
     */
    @Schema(description = "创建时间范围查询 - 开始时间", example = "2024-01-01 00:00:00")
    private String createdAtStart;

    /**
     * 创建时间范围 - 结束时间
     */
    @Schema(description = "创建时间范围查询 - 结束时间", example = "2024-12-31 23:59:59")
    private String createdAtEnd;
}
