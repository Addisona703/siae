package com.hngy.siae.attendance.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 考勤统计VO
 *
 * @author SIAE Team
 */
@Data
public class AttendanceStatisticsVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 统计月份(YYYY-MM)
     */
    private String statMonth;

    /**
     * 应出勤天数
     */
    private Integer totalDays;

    /**
     * 实际出勤天数
     */
    private Integer actualDays;

    /**
     * 迟到次数
     */
    private Integer lateCount;

    /**
     * 早退次数
     */
    private Integer earlyCount;

    /**
     * 缺勤次数
     */
    private Integer absenceCount;

    /**
     * 请假天数
     */
    private BigDecimal leaveDays;

    /**
     * 总考勤时长(分钟)
     */
    private Integer totalDurationMinutes;

    /**
     * 出勤率(%)
     */
    private BigDecimal attendanceRate;
}
