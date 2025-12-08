package com.hngy.siae.content.dto.request.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评论查询请求DTO
 *
 * @author KEYKB
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "评论查询请求")
public class CommentQueryDTO {

    @Schema(description = "评论用户ID", example = "10001")
    private Long userId;

    @Schema(description = "父评论ID，查询子评论时填写", example = "100")
    private Long parentId;

    @Schema(description = "内容ID，查询某内容下的评论", example = "1001")
    private Long contentId;

    @Schema(description = "排序字段", example = "createTime", allowableValues = {"createTime", "likeCount"})
    private String sortBy;

    @Schema(description = "排序方向", example = "desc", allowableValues = {"asc", "desc"})
    private String sortOrder;
}
