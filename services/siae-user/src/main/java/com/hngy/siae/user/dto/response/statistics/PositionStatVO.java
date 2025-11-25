package com.hngy.siae.user.dto.response.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 职位统计VO
 *
 * @author SIAE Team
 */
@Data
@Schema(description = "职位统计")
public class PositionStatVO {

    @Schema(description = "职位ID")
    private Long positionId;

    @Schema(description = "职位名称")
    private String positionName;

    @Schema(description = "人数")
    private Long count;
}
