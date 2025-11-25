package com.hngy.siae.user.dto.response.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 年级统计VO
 *
 * @author SIAE Team
 */
@Data
@Schema(description = "年级统计")
public class GradeStatVO {

    @Schema(description = "年级（如2024级）")
    private String grade;

    @Schema(description = "人数")
    private Long count;
}
