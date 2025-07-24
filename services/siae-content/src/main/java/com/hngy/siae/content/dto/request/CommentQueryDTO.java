package com.hngy.siae.content.dto.request;

import com.hngy.siae.content.common.enums.status.CommentStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评论查询DTO
 *
 * @author KEYKB
 * @date 2025/05/21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentQueryDTO {
    /**
     * 内容ID
     */
    private Long contentId;
    
    /**
     * 评论状态
     */
    private CommentStatusEnum status;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 父评论ID
     */
    private Long parentId;
}
