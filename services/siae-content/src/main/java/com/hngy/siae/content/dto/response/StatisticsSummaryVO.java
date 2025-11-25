package com.hngy.siae.content.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统计汇总数据VO
 * 用于首页统计卡片展示
 *
 * @author KEYKB
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "统计汇总数据")
public class StatisticsSummaryVO {

    @Schema(description = "总浏览量")
    private Long totalViews;

    @Schema(description = "总点赞数")
    private Long totalLikes;

    @Schema(description = "总收藏数")
    private Long totalFavorites;

    @Schema(description = "总评论数")
    private Long totalComments;

    @Schema(description = "内容总数")
    private Long totalContents;

    @Schema(description = "今日新增浏览量")
    private Long todayViews;

    @Schema(description = "今日新增点赞数")
    private Long todayLikes;

    @Schema(description = "今日新增收藏数")
    private Long todayFavorites;

    @Schema(description = "今日新增评论数")
    private Long todayComments;
}
