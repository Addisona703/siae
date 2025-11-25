package com.hngy.siae.attendance.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 部门考勤统计VO
 *
 * @author SIAE Team
 */
@Data
public class DepartmentStatisticsVO {

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 统计月份(YYYY-MM)
     */
    private String statMonth;

    /**
     * 部门总人数
     */
    private Integer totalMembers;

    /**
     * 平均出勤率(%)
     */
    private BigDecimal avgAttendanceRate;

    /**
     * 总迟到次数
     */
    private Integer totalLateCount;

    /**
     * 总早退次数
     */
    private Integer totalEarlyCount;

    /**
     * 总缺勤次数
     */
    private Integer totalAbsenceCount;

    /**
     * 总请假天数
     */
    private BigDecimal totalLeaveDays;

    /**
     * 成员统计列表
     */
    private List<AttendanceStatisticsVO> memberStatistics;
}
