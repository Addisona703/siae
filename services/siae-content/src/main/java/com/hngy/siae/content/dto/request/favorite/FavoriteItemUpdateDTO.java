package com.hngy.siae.content.dto.request.favorite;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新收藏请求DTO
 *
 * @author KEYKB
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "更新收藏请求")
public class FavoriteItemUpdateDTO {

    @NotNull(message = "收藏ID不能为空")
    @Schema(description = "收藏ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "移动到的收藏夹ID", example = "2")
    private Long folderId;

    @Size(max = 200, message = "收藏备注长度不能超过200个字符")
    @Schema(description = "收藏备注", example = "更新后的备注")
    private String note;

    @Schema(description = "排序序号", example = "1")
    private Integer sortOrder;
}
