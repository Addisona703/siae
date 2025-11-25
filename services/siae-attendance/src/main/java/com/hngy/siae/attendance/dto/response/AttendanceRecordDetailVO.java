package com.hngy.siae.attendance.dto.response;

import com.hngy.siae.attendance.enums.AttendanceStatus;
import com.hngy.siae.attendance.enums.AttendanceType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 考勤记录详细信息VO
 *
 * @author SIAE Team
 */
@Data
public class AttendanceRecordDetailVO {

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
     * 签到地点
     */
    private String checkInLocation;

    /**
     * 签退地点
     */
    private String checkOutLocation;

    /**
     * 考勤时长(分钟)
     */
    private Integer durationMinutes;

    /**
     * 考勤日期
     */
    private LocalDate attendanceDate;

    /**
     * 应用的规则ID
     */
    private Long ruleId;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 状态
     */
    private AttendanceStatus status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 关联的异常记录列表
     */
    private List<AttendanceAnomalyVO> anomalies;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
