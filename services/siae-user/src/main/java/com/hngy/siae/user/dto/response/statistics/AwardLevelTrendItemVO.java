package com.hngy.siae.user.dto.response.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 获奖趋势明细项（用于查询结果）
 *
 * @author SIAE Team
 */
@Data
@Schema(description = "获奖趋势明细项")
public class AwardLevelTrendItemVO {

    @Schema(description = "时间段（YYYY-MM或YYYY）")
    private String period;

    @Schema(description = "等级名称")
    private String levelName;

    @Schema(description = "数量")
    private Long count;
}
