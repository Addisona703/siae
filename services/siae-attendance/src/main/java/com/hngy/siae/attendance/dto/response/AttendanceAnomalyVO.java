package com.hngy.siae.attendance.dto.response;

import com.hngy.siae.attendance.enums.AnomalyType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤异常VO
 *
 * @author SIAE Team
 */
@Data
public class AttendanceAnomalyVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 关联的考勤记录ID
     */
    private Long attendanceRecordId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 异常类型
     */
    private AnomalyType anomalyType;

    /**
     * 异常日期
     */
    private LocalDate anomalyDate;

    /**
     * 异常时长(分钟)
     */
    private Integer durationMinutes;

    /**
     * 异常描述
     */
    private String description;

    /**
     * 是否已处理
     */
    private Boolean resolved;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 处理人名称
     */
    private String handlerName;

    /**
     * 处理说明
     */
    private String handlerNote;

    /**
     * 处理时间
     */
    private LocalDateTime handledAt;

    /**
     * 被请假抑制（请假申请ID）
     */
    private Long suppressedByLeave;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
