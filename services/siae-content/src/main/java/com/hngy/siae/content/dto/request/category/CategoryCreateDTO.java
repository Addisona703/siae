package com.hngy.siae.content.dto.request.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类创建请求DTO
 *
 * @author KEYKB
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "分类创建请求")
public class CategoryCreateDTO {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50个字符")
    @Schema(description = "分类名称", example = "技术文章", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "分类编码不能为空")
    @Size(max = 50, message = "分类编码长度不能超过50个字符")
    @Schema(description = "分类编码，唯一标识", example = "tech-article", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "父分类ID，创建子分类时填写", example = "1")
    private Long parentId;
}
