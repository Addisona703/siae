package com.hngy.siae.attendance.dto.request;

import com.hngy.siae.attendance.enums.RuleStatus;
import com.hngy.siae.attendance.enums.RuleTargetType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 更新考勤规则DTO
 *
 * @author SIAE Team
 */
@Data
public class AttendanceRuleUpdateDTO {

    /**
     * 规则名称
     */
    private String name;

    /**
     * 规则描述
     */
    private String description;

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
}
