package com.hngy.siae.attendance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考勤统计实体
 *
 * @author SIAE Team
 */
@Data
@TableName("attendance_statistics")
public class AttendanceStatistics {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

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

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
