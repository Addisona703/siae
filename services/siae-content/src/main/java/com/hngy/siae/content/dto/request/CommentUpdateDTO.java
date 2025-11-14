package com.hngy.siae.content.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评论更新DTO
 *
 * @author KEYKB
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentUpdateDTO {
    @NotBlank(message = "评论内容不能为空")
    private String content;
}
