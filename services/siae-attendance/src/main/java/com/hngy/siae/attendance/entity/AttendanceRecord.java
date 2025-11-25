package com.hngy.siae.attendance.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.hngy.siae.attendance.enums.AttendanceStatus;
import com.hngy.siae.attendance.enums.AttendanceType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤记录实体
 *
 * @author SIAE Team
 */
@Data
@TableName("attendance_record")
public class AttendanceRecord {

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
     * 考勤类型
     */
    private AttendanceType attendanceType;

    /**
     * 关联ID(活动考勤时存活动ID)
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
     * 状态
     */
    private AttendanceStatus status;

    /**
     * 备注
     */
    private String remark;

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
