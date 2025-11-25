package com.hngy.siae.attendance.dto.request;

import com.hngy.siae.attendance.enums.AttendanceType;
import com.hngy.siae.attendance.enums.RuleTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 创建考勤规则DTO
 *
 * @author SIAE Team
 */
@Data
public class AttendanceRuleCreateDTO {

    /**
     * 规则名称
     */
    @NotBlank(message = "规则名称不能为空")
    private String name;

    /**
     * 规则描述
     */
    private String description;

    /**
     * 考勤类型
     */
    @NotNull(message = "考勤类型不能为空")
    private AttendanceType attendanceType;

    /**
     * 关联ID（活动考勤时存活动ID）
     */
    private Long relatedId;

    /**
     * 适用对象类型
     */
    @NotNull(message = "适用对象类型不能为空")
    private RuleTargetType targetType;

    /**
     * 适用对象ID列表
     */
    private List<Long> targetIds;

    /**
     * 签到开始时间
     */
    @NotNull(message = "签到开始时间不能为空")
    private LocalTime checkInStartTime;

    /**
     * 签到结束时间
     */
    @NotNull(message = "签到结束时间不能为空")
    private LocalTime checkInEndTime;

    /**
     * 签退开始时间
     */
    @NotNull(message = "签退开始时间不能为空")
    private LocalTime checkOutStartTime;

    /**
     * 签退结束时间
     */
    @NotNull(message = "签退结束时间不能为空")
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
     * 允许的位置列表（JSON格式）
     */
    private String allowedLocations;

    /**
     * 位置半径(米)
     */
    private Integer locationRadiusMeters;

    /**
     * 生效日期
     */
    @NotNull(message = "生效日期不能为空")
    private LocalDate effectiveDate;

    /**
     * 失效日期
     */
    private LocalDate expiryDate;

    /**
     * 优先级
     */
    private Integer priority;
}
