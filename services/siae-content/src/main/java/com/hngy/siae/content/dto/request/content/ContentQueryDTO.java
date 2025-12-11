package com.hngy.siae.content.dto.request.content;

import com.hngy.siae.content.enums.ContentTypeEnum;
import com.hngy.siae.content.enums.status.ContentStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 内容查询请求DTO
 *
 * @author KEYKB
 */
@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "内容查询请求")
public class ContentQueryDTO {

    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "标签ID列表", example = "[1, 2, 3]")
    private List<Long> tagIds;

    @Schema(description = "查询指定用户发布的内容", example = "1")
    private Long uploadedBy;

    @Schema(description = "内容类型：ARTICLE-文章，NOTE-笔记，QUESTION-问题，VIDEO-视频，FILE-文件", example = "ARTICLE")
    private ContentTypeEnum type;

    @Schema(description = "内容状态：DRAFT-草稿，PENDING-待审核，PUBLISHED-已发布", example = "PUBLISHED")
    private ContentStatusEnum status;

    @Schema(description = "搜索关键词", example = "Spring Boot")
    private String keyword;
}
