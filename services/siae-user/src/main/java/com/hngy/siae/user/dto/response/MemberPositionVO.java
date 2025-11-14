package com.hngy.siae.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 成员职位视图
 *
 * @author KEYKB
 */
@Data
@Schema(description = "成员职位视图")
public class MemberPositionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "职位ID")
    private Long positionId;

    @Schema(description = "职位名称")
    private String positionName;

    @Schema(description = "所属部门ID，NULL 表示全协会职位")
    private Long departmentId;

    @Schema(description = "所属部门名称")
    private String departmentName;

    @Schema(description = "任职开始日期")
    private LocalDate startDate;

    @Schema(description = "任职结束日期")
    private LocalDate endDate;
}
