package com.hngy.siae.attendance.dto.response;

import com.hngy.siae.attendance.enums.AttendanceStatus;
import com.hngy.siae.attendance.enums.AttendanceType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤记录VO（简要信息）
 *
 * @author SIAE Team
 */
@Data
public class AttendanceRecordVO {

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
     * 用户头像URL
     */
    private String userAvatarUrl;

    /**
     * 考勤类型
     */
    private AttendanceType attendanceType;

    /**
     * 关联ID（活动考勤时存活动ID）
     */
    private Long relatedId;

    /**
     * 签到时间
     */
    private LocalDateTime checkInTime;

    /**
     * 签退时间
     */
    private LocalDateTime checkOutTime;

    /**
     * 考勤时长(分钟)
     */
    private Integer durationMinutes;

    /**
     * 考勤日期
     */
    private LocalDate attendanceDate;

    /**
     * 状态
     */
    private AttendanceStatus status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
