package com.hngy.siae.user.dto.response.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 专业统计VO
 *
 * @author SIAE Team
 */
@Data
@Schema(description = "专业统计")
public class MajorStatVO {

    @Schema(description = "专业ID")
    private Long majorId;

    @Schema(description = "专业名称")
    private String majorName;

    @Schema(description = "人数")
    private Long count;
}
