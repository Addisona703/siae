package com.hngy.siae.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量分配角色请求DTO
 * 
 * @author KEYKB
 */
@Data
@Schema(description = "批量分配角色请求")
public class BatchAssignRoleDTO {
    
    /**
     * 用户ID列表
     */
    @NotEmpty(message = "用户ID列表不能为空")
    @Schema(description = "用户ID列表", example = "[1, 2, 3]")
    private List<Long> userIds;
}
