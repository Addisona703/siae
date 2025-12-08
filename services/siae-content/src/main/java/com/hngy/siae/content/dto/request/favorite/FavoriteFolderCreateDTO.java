package com.hngy.siae.content.dto.request.favorite;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建收藏夹请求DTO
 *
 * @author KEYKB
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "创建收藏夹请求")
public class FavoriteFolderCreateDTO {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "10001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @NotBlank(message = "收藏夹名称不能为空")
    @Size(max = 50, message = "收藏夹名称长度不能超过50个字符")
    @Schema(description = "收藏夹名称", example = "技术学习", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 200, message = "收藏夹描述长度不能超过200个字符")
    @Schema(description = "收藏夹描述", example = "收藏的技术类文章和资源")
    private String description;

    @Schema(description = "是否公开：0-私密，1-公开", example = "0")
    private Integer isPublic;
}
