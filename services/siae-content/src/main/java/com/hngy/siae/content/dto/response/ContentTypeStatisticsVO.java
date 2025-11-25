package com.hngy.siae.content.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 内容类型统计数据VO
 * 用于饼图展示各类型内容的分布
 *
 * @author KEYKB
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "内容类型统计数据")
public class ContentTypeStatisticsVO {

    @Schema(description = "内容类型", example = "article")
    private String contentType;

    @Schema(description = "类型名称", example = "文章")
    private String typeName;

    @Schema(description = "内容数量")
    private Long contentCount;

    @Schema(description = "总浏览量")
    private Long totalViews;

    @Schema(description = "总点赞数")
    private Long totalLikes;
}
