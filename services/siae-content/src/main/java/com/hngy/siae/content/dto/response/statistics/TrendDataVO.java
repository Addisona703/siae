package com.hngy.siae.content.dto.response.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 趋势数据 VO
 * 用于绘制折线图、柱状图等
 *
 * @author KEYKB
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "趋势数据响应对象")
public class TrendDataVO {

    @Schema(description = "日期列表（X轴）", example = "[\"2024-01-01\", \"2024-01-02\", \"2024-01-03\"]")
    private List<String> dates;

    @Schema(description = "浏览量数据（Y轴）", example = "[100, 150, 200]")
    private List<Long> viewCounts;

    @Schema(description = "点赞量数据（Y轴）", example = "[10, 15, 20]")
    private List<Long> likeCounts;

    @Schema(description = "收藏量数据（Y轴）", example = "[5, 8, 12]")
    private List<Long> favoriteCounts;

    @Schema(description = "评论量数据（Y轴）", example = "[3, 5, 7]")
    private List<Long> commentCounts;
}
