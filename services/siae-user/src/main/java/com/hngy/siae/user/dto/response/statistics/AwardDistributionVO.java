package com.hngy.siae.user.dto.response.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 获奖分布统计VO（按等级或类型）
 *
 * @author SIAE Team
 */
@Data
@Schema(description = "获奖分布统计")
public class AwardDistributionVO {

    @Schema(description = "等级/类型ID")
    private Long id;

    @Schema(description = "等级/类型名称")
    private String name;

    @Schema(description = "获奖数量")
    private Long count;
}
