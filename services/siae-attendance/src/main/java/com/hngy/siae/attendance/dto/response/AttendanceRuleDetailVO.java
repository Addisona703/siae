package com.hngy.siae.attendance.dto.response;

import com.hngy.siae.attendance.entity.AttendanceRule;
import com.hngy.siae.attendance.enums.AttendanceType;
import com.hngy.siae.attendance.enums.RuleStatus;
import com.hngy.siae.attendance.enums.RuleTargetType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 考勤规则详细信息VO
 *
 * @author SIAE Team
 */
@Data
public class AttendanceRuleDetailVO {

    /**
     * 主键ID
     */
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
     * 关联ID（活动考勤时存活动ID）
     */
    private Long relatedId;

    /**
     * 适用对象类型
     */
    private RuleTargetType targetType;

    /**
     * 适用对象ID列表
     */
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
    private List<AttendanceRule.Location> allowedLocations;

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
     * 优先级
     */
    private Integer priority;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 创建人名称
     */
    private String creatorName;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
