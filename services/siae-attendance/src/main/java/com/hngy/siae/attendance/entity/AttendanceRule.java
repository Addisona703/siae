package com.hngy.siae.attendance.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.hngy.siae.attendance.enums.AttendanceType;
import com.hngy.siae.attendance.enums.RuleStatus;
import com.hngy.siae.attendance.enums.RuleTargetType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 考勤规则实体
 *
 * @author SIAE Team
 */
@Data
@TableName(value = "attendance_rule", autoResultMap = true)
public class AttendanceRule {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 规则名称
     */
    private String name;

    /**
     * 规则描述
     */
    private String description;

    /**
     * 考勤类型
     */
    private AttendanceType attendanceType;

    /**
     * 关联ID(活动考勤时存活动ID)
     */
    private Long relatedId;

    /**
     * 适用对象类型
     */
    private RuleTargetType targetType;

    /**
     * 适用对象ID列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> targetIds;

    /**
     * 签到开始时间
     */
    private LocalTime checkInStartTime;

    /**
     * 签到结束时间
     */
    private LocalTime checkInEndTime;

    /**
     * 签退开始时间
     */
    private LocalTime checkOutStartTime;

    /**
     * 签退结束时间
     */
    private LocalTime checkOutEndTime;

    /**
     * 迟到阈值(分钟)
     */
    private Integer lateThresholdMinutes;

    /**
     * 早退阈值(分钟)
     */
    private Integer earlyThresholdMinutes;

    /**
     * 是否需要位置验证
     */
    private Boolean locationRequired;

    /**
     * 允许的位置列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Location> allowedLocations;

    /**
     * 位置半径(米)
     */
    private Integer locationRadiusMeters;

    /**
     * 生效日期
     */
    private LocalDate effectiveDate;

    /**
     * 失效日期
     */
    private LocalDate expiryDate;

    /**
     * 状态
     */
    private RuleStatus status;

    /**
     * 优先级(数字越大优先级越高)
     */
    private Integer priority;

    /**
     * 创建人ID
     */
    private Long createdBy;

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

    /**
     * 位置信息内部类
     */
    @Data
    public static class Location {
        /**
         * 位置名称
         */
        private String name;

        /**
         * 纬度
         */
        private Double latitude;

        /**
         * 经度
         */
        private Double longitude;
    }
}
