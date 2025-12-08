package com.hngy.siae.content.dto.response.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统计汇总数据 VO
 * 用于首页统计卡片展示
 *
 * @author KEYKB
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "统计汇总数据响应对象")
public class StatisticsSummaryVO {

    @Schema(description = "总浏览量", example = "1234567")
    private Long totalViews;

    @Schema(description = "总点赞数", example = "56789")
    private Long totalLikes;

    @Schema(description = "总收藏数", example = "12345")
    private Long totalFavorites;

    @Schema(description = "总评论数", example = "6789")
    private Long totalComments;

    @Schema(description = "内容总数", example = "1024")
    private Long totalContents;

    @Schema(description = "今日新增浏览量", example = "1234")
    private Long todayViews;

    @Schema(description = "今日新增点赞数", example = "56")
    private Long todayLikes;

    @Schema(description = "今日新增收藏数", example = "23")
    private Long todayFavorites;

    @Schema(description = "今日新增评论数", example = "12")
    private Long todayComments;
}
