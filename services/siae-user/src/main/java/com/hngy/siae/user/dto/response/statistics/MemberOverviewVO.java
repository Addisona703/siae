package com.hngy.siae.user.dto.response.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 成员概览统计VO
 *
 * @author SIAE Team
 */
@Data
@Schema(description = "成员概览统计")
public class MemberOverviewVO {

    @Schema(description = "总用户数")
    private Long totalUsers;

    @Schema(description = "启用用户数")
    private Long enabledUsers;

    @Schema(description = "禁用用户数")
    private Long disabledUsers;

    @Schema(description = "正式成员数")
    private Long formalMembers;

    @Schema(description = "候选成员数")
    private Long candidateMembers;

    @Schema(description = "本月新增用户")
    private Long newUsersThisMonth;

    @Schema(description = "本年新增用户")
    private Long newUsersThisYear;
}
