package com.hngy.siae.content.dto.request.favorite;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 添加收藏请求DTO
 *
 * @author KEYKB
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "添加收藏请求")
public class FavoriteItemAddDTO {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "10001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @NotNull(message = "内容ID不能为空")
    @Schema(description = "内容ID", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long contentId;

    @Schema(description = "收藏夹ID，不传则添加到默认收藏夹", example = "1")
    private Long folderId;

    @Size(max = 200, message = "收藏备注长度不能超过200个字符")
    @Schema(description = "收藏备注", example = "很实用的教程")
    private String note;
}
