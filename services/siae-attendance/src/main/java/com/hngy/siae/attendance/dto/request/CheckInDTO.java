package com.hngy.siae.attendance.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
     * 签到位置信息
     */
    @Valid
    private LocationInfo location;

    /**
     * 考勤类型（0-日常考勤，1-活动考勤）
     */
    private Integer attendanceType;

    /**
     * 关联ID（活动考勤时存活动ID）
     */
    private Long relatedId;

    /**
     * 位置信息内部类
     */
    @Data
    public static class LocationInfo {
        /**
         * 位置名称
         */
        private String name;

        /**
         * 纬度（范围：-90 到 90）
         */
        @DecimalMin(value = "-90.0", message = "纬度必须在 -90 到 90 之间")
        @DecimalMax(value = "90.0", message = "纬度必须在 -90 到 90 之间")
        private Double latitude;

        /**
         * 经度（范围：-180 到 180）
         */
        @DecimalMin(value = "-180.0", message = "经度必须在 -180 到 180 之间")
        @DecimalMax(value = "180.0", message = "经度必须在 -180 到 180 之间")
        private Double longitude;
    }
}
