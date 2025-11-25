package com.hngy.siae.user.dto.response.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 部门统计VO
 *
 * @author SIAE Team
 */
@Data
@Schema(description = "部门统计")
public class DepartmentStatVO {

    @Schema(description = "部门ID")
    private Long departmentId;

    @Schema(description = "部门名称")
    private String departmentName;

    @Schema(description = "总成员数")
    private Long totalMembers;

    @Schema(description = "正式成员数")
    private Long formalMembers;

    @Schema(description = "候选成员数")
    private Long candidateMembers;

    @Schema(description = "有职位的人数")
    private Long withPosition;
}
