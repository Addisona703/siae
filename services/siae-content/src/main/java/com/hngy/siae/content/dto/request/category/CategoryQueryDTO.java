package com.hngy.siae.content.dto.request.category;

import com.hngy.siae.content.enums.status.CategoryStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类查询DTO
 * 
 * @author KEYKB
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "分类查询参数")
public class CategoryQueryDTO {
    
    /**
     * 关键词（模糊搜索分类名称或编码）
     */
    @Schema(description = "关键词，模糊匹配分类名称或编码", example = "软件")
    private String keyword;
    
    /**
     * 分类状态
     */
    @Schema(description = "分类状态：ENABLED-启用, DISABLED-禁用")
    private CategoryStatusEnum status;
    
    /**
     * 父分类ID
     */
    @Schema(description = "父分类ID，查询指定父分类下的子分类", example = "1")
    private Long parentId;
    
    /**
     * 创建时间范围 - 开始时间
     */
    @Schema(description = "创建时间范围-开始时间", example = "2024-01-01 00:00:00")
    private String createdAtStart;
    
    /**
     * 创建时间范围 - 结束时间
     */
    @Schema(description = "创建时间范围-结束时间", example = "2024-12-31 23:59:59")
    private String createdAtEnd;
}
