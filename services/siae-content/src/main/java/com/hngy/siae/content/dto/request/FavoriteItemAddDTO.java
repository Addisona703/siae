package com.hngy.siae.content.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 添加收藏请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "添加收藏请求对象")
public class FavoriteItemAddDTO {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @NotNull(message = "内容ID不能为空")
    @Schema(description = "内容ID", example = "1")
    private Long contentId;

    @Schema(description = "收藏夹ID，不传则添加到默认收藏夹", example = "1")
    private Long folderId;

    @Schema(description = "收藏备注", example = "很实用的教程")
    private String note;
}
