package com.hngy.siae.content.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评论创建DTO
 *
 * @author KEYKB
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentCreateDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    private Long parentId;
    
    @NotBlank(message = "评论内容不能为空")
    private String content;
}
