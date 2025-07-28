package com.hngy.siae.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 权限查询请求DTO
 * 
 * @author KEYKB
 */
@Data
@Schema(description = "权限查询请求参数")
public class PermissionQueryDTO {
    
    /**
     * 权限名称（模糊查询）
     */
    @Schema(description = "权限名称，支持模糊查询", example = "用户管理")
    private String name;
    
    /**
     * 权限编码（模糊查询）
     */
    @Schema(description = "权限编码，支持模糊查询", example = "user:manage")
    private String code;
    
    /**
     * 权限类型
     */
    @Schema(description = "权限类型：menu菜单、button按钮", example = "menu")
    private String type;
    
    /**
     * 父权限ID
     */
    @Schema(description = "父权限ID，查询指定父级下的权限", example = "1")
    private Long parentId;
    
    /**
     * 状态：0禁用，1启用
     */
    @Schema(description = "权限状态：0禁用，1启用", example = "1")
    private Integer status;
    
    /**
     * 创建时间范围 - 开始时间
     */
    @Schema(description = "创建时间范围查询 - 开始时间", example = "2024-01-01 00:00:00")
    private String createAtStart;
    
    /**
     * 创建时间范围 - 结束时间
     */
    @Schema(description = "创建时间范围查询 - 结束时间", example = "2024-12-31 23:59:59")
    private String createAtEnd;
}
