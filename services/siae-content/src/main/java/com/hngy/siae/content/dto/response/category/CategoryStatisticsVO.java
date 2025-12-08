package com.hngy.siae.content.dto.response.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类统计数据 VO
 * 用于饼图、柱状图展示各分类的内容分布
 *
 * @author KEYKB
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "分类统计数据响应对象")
public class CategoryStatisticsVO {

    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "分类名称", example = "技术文章")
    private String categoryName;

    @Schema(description = "内容数量", example = "128")
    private Long contentCount;

    @Schema(description = "总浏览量", example = "56789")
    private Long totalViews;

    @Schema(description = "总点赞数", example = "1234")
    private Long totalLikes;

    @Schema(description = "总收藏数", example = "567")
    private Long totalFavorites;

    @Schema(description = "总评论数", example = "890")
    private Long totalComments;
}
