package com.hngy.siae.content.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类统计数据VO
 * 用于饼图、柱状图展示各分类的内容分布
 *
 * @author KEYKB
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "分类统计数据")
public class CategoryStatisticsVO {

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "内容数量")
    private Long contentCount;

    @Schema(description = "总浏览量")
    private Long totalViews;

    @Schema(description = "总点赞数")
    private Long totalLikes;

    @Schema(description = "总收藏数")
    private Long totalFavorites;

    @Schema(description = "总评论数")
    private Long totalComments;
}
