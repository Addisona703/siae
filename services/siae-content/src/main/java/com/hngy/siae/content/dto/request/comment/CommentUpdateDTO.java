package com.hngy.siae.content.dto.request.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评论更新请求DTO
 *
 * @author KEYKB
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "评论更新请求")
public class CommentUpdateDTO {

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容长度不能超过1000个字符")
    @Schema(description = "评论内容", example = "更新后的评论内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
}
