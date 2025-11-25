package com.hngy.siae.attendance.dto.request;

import com.hngy.siae.attendance.enums.LeaveStatus;
import com.hngy.siae.attendance.enums.LeaveType;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 请假查询条件DTO
 *
 * @author SIAE Team
 */
@Data
public class LeaveQueryDTO {

    /**
     * 申请人ID
     */
    private Long userId;

    /**
     * 申请人ID列表
     */
    private List<Long> userIds;

    /**
     * 请假类型
     */
    private LeaveType leaveType;

    /**
     * 请假状态
     */
    private LeaveStatus status;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 关键字搜索
     */
    private String keyword;
}
