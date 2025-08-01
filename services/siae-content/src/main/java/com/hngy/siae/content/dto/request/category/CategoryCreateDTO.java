package com.hngy.siae.content.dto.request.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类创建DTO
 * 
 * @author KEYKB
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryCreateDTO {
    
    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50个字符")
    private String name;
    
    /**
     * 分类编码
     */
    @NotBlank(message = "分类编码不能为空")
    @Size(max = 50, message = "分类编码长度不能超过50个字符")
    private String code;
    
    /**
     * 父分类ID（可选）
     */
    private Long parentId;
}
