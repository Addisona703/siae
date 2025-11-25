package com.hngy.siae.attendance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 活动考勤统计VO
 *
 * @author SIAE Team
 */
@Data
@Schema(description = "活动考勤统计信息")
public class ActivityAttendanceStatisticsVO {

    /**
     * 活动ID
     */
    @Schema(description = "活动ID")
    private Long activityId;

    /**
     * 总记录数
     */
    @Schema(description = "总记录数")
    private Integer totalRecords;

    /**
     * 签到人数
     */
    @Schema(description = "签到人数")
    private Integer checkInCount;

    /**
     * 签退人数
     */
    @Schema(description = "签退人数")
    private Integer checkOutCount;

    /**
     * 完成考勤人数（签到且签退）
     */
    @Schema(description = "完成考勤人数（签到且签退）")
    private Integer completedCount;

    /**
     * 异常记录数
     */
    @Schema(description = "异常记录数")
    private Integer abnormalCount;

    /**
     * 平均考勤时长（分钟）
     */
    @Schema(description = "平均考勤时长（分钟）")
    private Integer averageDurationMinutes;
}
