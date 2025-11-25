package com.hngy.siae.user.dto.response.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 入会趋势统计VO
 *
 * @author SIAE Team
 */
@Data
@Schema(description = "入会趋势统计")
public class MembershipTrendVO {

    @Schema(description = "时间段（YYYY-MM或YYYY）")
    private String period;

    @Schema(description = "转正人数")
    private Long formalCount;

    @Schema(description = "新增候选人数")
    private Long candidateCount;
}
