package com.hngy.siae.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 权限树结构批量更新请求DTO
 * 
 * 用于支持前端拖拽操作后修改权限的层级依赖关系
 * 
 * @author SIAE开发团队
 */
@Data
@Schema(description = "权限树结构批量更新请求参数")
public class PermissionTreeUpdateDTO {
    
    /**
     * 权限ID
     */
    @NotNull(message = "权限ID不能为空")
    @Schema(description = "权限ID", example = "1")
    private Long id;
    
    /**
     * 新的父权限ID
     * 根节点传null或0
     */
    @Schema(description = "新的父权限ID，根节点传null或0", example = "0")
    private Long parentId;
    
    /**
     * 新的排序值
     * 同级权限按此值升序排列
     */
    @NotNull(message = "排序值不能为空")
    @Schema(description = "新的排序值，同级权限按此值升序排列", example = "1")
    private Integer sortOrder;
    
    /**
     * 层级深度
     * 用于前端验证，可选字段
     */
    @Schema(description = "层级深度，用于前端验证", example = "0")
    private Integer level;
}
