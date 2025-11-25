package com.hngy.siae.attendance.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.hngy.siae.attendance.enums.AnomalyType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤异常实体
 *
 * @author SIAE Team
 */
@Data
@TableName("attendance_anomaly")
public class AttendanceAnomaly {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
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
     * 处理说明
     */
    private String handlerNote;

    /**
     * 处理时间
     */
    private LocalDateTime handledAt;

    /**
     * 被请假抑制(请假申请ID)
     */
    private Long suppressedByLeave;

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

    /**
     * 逻辑删除标记
     */
    @TableLogic
    private Integer deleted;
}
