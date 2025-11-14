package com.hngy.siae.content.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新收藏夹请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "更新收藏夹请求对象")
public class FavoriteFolderUpdateDTO {

    @NotNull(message = "收藏夹ID不能为空")
    @Schema(description = "收藏夹ID", example = "1")
    private Long id;

    @Schema(description = "收藏夹名称", example = "技术学习")
    private String name;

    @Schema(description = "收藏夹描述", example = "收藏的技术类文章和资源")
    private String description;

    @Schema(description = "是否公开（0私密，1公开）", example = "1")
    private Integer isPublic;

    @Schema(description = "排序序号", example = "1")
    private Integer sortOrder;
}
