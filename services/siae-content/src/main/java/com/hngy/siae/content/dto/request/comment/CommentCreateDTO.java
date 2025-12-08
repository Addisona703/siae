package com.hngy.siae.content.dto.request.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评论创建请求DTO
 *
 * @author KEYKB
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "评论创建请求")
public class CommentCreateDTO {

    @Schema(description = "评论用户ID（由后端自动填充，无需前端传入）", example = "10001", hidden = true)
    private Long userId;

    @Schema(description = "父评论ID，回复评论时填写", example = "100")
    private Long parentId;

    @Schema(description = "回复目标用户ID，回复某人时填写", example = "10002")
    private Long replyToUserId;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容长度不能超过1000个字符")
    @Schema(description = "评论内容", example = "这篇文章写得很好，学到了很多！", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
}
