package com.hngy.siae.content.dto.request.interaction;

import com.hngy.siae.content.enums.ActionTypeEnum;
import com.hngy.siae.content.enums.TypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户行为请求DTO
 *
 * @author KEYKB
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户行为请求")
public class ActionDTO {

    @Schema(description = "用户ID（由后端自动填充）", example = "10001", accessMode = Schema.AccessMode.READ_ONLY)
    private Long userId;

    @NotNull(message = "目标ID不能为空")
    @Schema(description = "目标ID", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long targetId;

    @NotNull(message = "目标类型不能为空")
    @Schema(description = "目标类型：CONTENT-内容，COMMENT-评论", example = "CONTENT", requiredMode = Schema.RequiredMode.REQUIRED)
    private TypeEnum targetType;

    @NotNull(message = "行为类型不能为空")
    @Schema(description = "行为类型：LIKE-点赞，FAVORITE-收藏，VIEW-浏览", example = "LIKE", requiredMode = Schema.RequiredMode.REQUIRED)
    private ActionTypeEnum actionType;
}
