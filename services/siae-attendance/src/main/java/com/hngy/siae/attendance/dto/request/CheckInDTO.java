package com.hngy.siae.attendance.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 签到请求DTO
 *
 * @author SIAE Team
 */
@Data
public class CheckInDTO {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 签到时间
     */
    @NotNull(message = "签到时间不能为空")
    private LocalDateTime timestamp;

    /**
     * 签到地点
     */
    private String location;

    /**
     * 考勤类型（0-日常考勤，1-活动考勤）
     */
    private Integer attendanceType;

    /**
     * 关联ID（活动考勤时存活动ID）
     */
    private Long relatedId;
}
