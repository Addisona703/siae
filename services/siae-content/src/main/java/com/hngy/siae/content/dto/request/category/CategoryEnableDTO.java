package com.hngy.siae.content.dto.request.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类启用/禁用请求DTO
 *
 * @author KEYKB
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "分类启用/禁用请求")
public class CategoryEnableDTO {

    @NotNull(message = "分类ID不能为空")
    @Schema(description = "分类ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @NotNull(message = "启用状态不能为空")
    @Schema(description = "是否启用：true-启用，false-禁用", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean enable;
}
