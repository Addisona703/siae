package com.hngy.siae.user.dto.response.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 获奖概览统计VO
 *
 * @author SIAE Team
 */
@Data
@Schema(description = "获奖概览统计")
public class AwardOverviewVO {

    @Schema(description = "总获奖数")
    private Long totalAwards;

    @Schema(description = "本年获奖数")
    private Long thisYearAwards;

    @Schema(description = "本月获奖数")
    private Long thisMonthAwards;

    @Schema(description = "获奖总人数")
    private Long totalAwardedUsers;

    @Schema(description = "团队获奖数（成员数>1）")
    private Long teamAwards;

    @Schema(description = "个人获奖数（成员数=1）")
    private Long individualAwards;
}
