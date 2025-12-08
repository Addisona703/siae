package com.hngy.siae.content.dto.response.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 内容统计信息响应 VO
 *
 * @author KEYKB
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "内容统计信息响应对象")
public class StatisticsVO {

    @Schema(description = "内容ID", example = "1001")
    private Long contentId;

    @Schema(description = "浏览次数", example = "12580")
    private Integer viewCount;

    @Schema(description = "点赞次数", example = "1234")
    private Integer likeCount;

    @Schema(description = "收藏次数", example = "567")
    private Integer favoriteCount;

    @Schema(description = "评论次数", example = "89")
    private Integer commentCount;
}
