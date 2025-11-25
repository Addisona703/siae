package com.hngy.siae.user.dto.response.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 性别统计VO
 *
 * @author SIAE Team
 */
@Data
@Schema(description = "性别统计")
public class GenderStatVO {

    @Schema(description = "性别代码（0未知，1男，2女）")
    private Integer gender;

    @Schema(description = "性别名称")
    private String genderName;

    @Schema(description = "人数")
    private Long count;

    @Schema(description = "占比百分比")
    private Double percentage;
}
