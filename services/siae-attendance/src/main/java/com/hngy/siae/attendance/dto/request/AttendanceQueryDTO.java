package com.hngy.siae.attendance.dto.request;

import com.hngy.siae.attendance.enums.AttendanceStatus;
import com.hngy.siae.attendance.enums.AttendanceType;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤查询条件DTO
 *
 * @author SIAE Team
 */
@Data
public class AttendanceQueryDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户ID列表
     */
    private List<Long> userIds;

    /**
     * 考勤类型
     */
    private AttendanceType attendanceType;

    /**
     * 关联ID（活动考勤时的活动ID）
     */
    private Long relatedId;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 考勤状态
     */
    private AttendanceStatus status;

    /**
     * 关键字搜索（用户名、备注等）
     */
    private String keyword;
}
