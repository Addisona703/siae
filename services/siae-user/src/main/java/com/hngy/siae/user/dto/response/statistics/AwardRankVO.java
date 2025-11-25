package com.hngy.siae.user.dto.response.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 获奖排行榜VO
 *
 * @author SIAE Team
 */
@Data
@Schema(description = "获奖排行榜")
public class AwardRankVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "头像URL")
    private String avatarUrl;

    @Schema(description = "获奖总数")
    private Long awardCount;

    @Schema(description = "国家级获奖数")
    private Long nationalCount;

    @Schema(description = "省级获奖数")
    private Long provincialCount;

    @Schema(description = "校级获奖数")
    private Long schoolCount;
}
