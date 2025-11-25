package com.hngy.siae.attendance.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤记录导出DTO
 *
 * @author SIAE Team
 */
@Data
public class AttendanceExportDTO {

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 成员ID列表
     */
    private List<Long> memberIds;

    /**
     * 导出格式（csv, excel）
     */
    private String format;
}
