package com.hngy.siae.content.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新收藏请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "更新收藏请求对象")
public class FavoriteItemUpdateDTO {

    @NotNull(message = "收藏ID不能为空")
    @Schema(description = "收藏ID", example = "1")
    private Long id;

    @Schema(description = "移动到的收藏夹ID", example = "2")
    private Long folderId;

    @Schema(description = "收藏备注", example = "更新后的备注")
    private String note;

    @Schema(description = "排序序号", example = "1")
    private Integer sortOrder;
}
