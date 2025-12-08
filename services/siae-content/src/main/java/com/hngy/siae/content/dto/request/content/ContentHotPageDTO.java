package com.hngy.siae.content.dto.request.content;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.content.enums.ContentTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 热门内容分页查询请求DTO
 *
 * @author KEYKB
 */
@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "热门内容分页查询请求")
public class ContentHotPageDTO extends PageDTO<Object> {

    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "内容类型：ARTICLE-文章，NOTE-笔记，QUESTION-问题，VIDEO-视频，FILE-文件", example = "ARTICLE")
    private ContentTypeEnum type;

    @Schema(description = "排序字段：viewCount-浏览量，likeCount-点赞数，commentCount-评论数", example = "viewCount")
    private String sortBy;
}
