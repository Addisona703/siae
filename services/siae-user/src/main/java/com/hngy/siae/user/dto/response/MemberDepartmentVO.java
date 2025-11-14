package com.hngy.siae.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 成员部门视图
 *
 * @author KEYKB
 */
@Data
@Schema(description = "成员部门视图")
public class MemberDepartmentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "部门ID")
    private Long departmentId;

    @Schema(description = "部门名称")
    private String departmentName;

    @Schema(description = "加入日期")
    private LocalDate joinDate;

    @Schema(description = "是否在该部门担任职位")
    private Boolean hasPosition;
}
