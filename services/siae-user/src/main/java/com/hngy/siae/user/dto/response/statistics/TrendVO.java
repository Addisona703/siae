package com.hngy.siae.user.dto.response.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 趋势统计VO（通用）
 *
 * @author SIAE Team
 */
@Data
@Schema(description = "趋势统计")
public class TrendVO {

    @Schema(description = "时间段（YYYY-MM或YYYY）")
    private String period;

    @Schema(description = "数量")
    private Long count;
}
