package com.hngy.siae.user.dto.response.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 获奖趋势详细统计VO（包含各等级分布）
 *
 * @author SIAE Team
 */
@Data
@Schema(description = "获奖趋势详细统计")
public class AwardTrendDetailVO {

    @Schema(description = "时间段（YYYY-MM或YYYY）")
    private String period;

    @Schema(description = "总数量")
    private Long totalCount;

    @Schema(description = "各等级获奖数量，key为等级名称，value为数量")
    private Map<String, Long> levelCounts;
}
