package com.hngy.siae.attendance.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 报表生成DTO
 *
 * @author SIAE Team
 */
@Data
public class ReportGenerateDTO {

    /**
     * 报表类型（monthly, anomaly, department）
     */
    @NotNull(message = "报表类型不能为空")
    private String reportType;

    /**
     * 开始日期
     */
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    /**
     * 部门ID列表
     */
    private List<Long> departmentIds;

    /**
     * 用户ID列表
     */
    private List<Long> userIds;

    /**
     * 导出格式（csv, pdf）
     */
    private String format;
}
