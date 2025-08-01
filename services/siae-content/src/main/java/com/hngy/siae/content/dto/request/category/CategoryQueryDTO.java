package com.hngy.siae.content.dto.request.category;

import com.hngy.siae.content.common.enums.status.CategoryStatusEnum;
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
public class CategoryQueryDTO {
    
    /**
     * 关键词（用于搜索分类名称或编码）
     */
    private String keyword;
    
    /**
     * 分类名称（精确匹配）
     */
    private String name;
    
    /**
     * 分类编码（精确匹配）
     */
    private String code;
    
    /**
     * 分类状态
     */
    private CategoryStatusEnum status;
    
    /**
     * 父分类ID
     */
    private Long parentId;
    
    /**
     * 创建时间范围 - 开始时间
     */
    private String createdAtStart;
    
    /**
     * 创建时间范围 - 结束时间
     */
    private String createdAtEnd;
}
