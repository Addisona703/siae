package com.hngy.siae.content.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评论查询DTO
 *
 * @author KEYKB
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentQueryDTO {
    private Long userId;
    private Long parentId;
    private Long contentId;
}
