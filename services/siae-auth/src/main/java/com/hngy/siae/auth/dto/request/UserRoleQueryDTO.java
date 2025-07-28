package com.hngy.siae.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户角色查询DTO
 *
 * @author KEYKB
 */
@Data
@Schema(description = "用户角色查询条件")
public class UserRoleQueryDTO {

    /**
     * 角色ID（精确查询）
     */
    @Schema(description = "角色ID", example = "1")
    private Long roleId;
    
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
