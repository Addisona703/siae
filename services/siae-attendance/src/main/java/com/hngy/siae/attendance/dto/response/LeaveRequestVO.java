package com.hngy.siae.attendance.dto.response;

import com.hngy.siae.attendance.enums.LeaveStatus;
import com.hngy.siae.attendance.enums.LeaveType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 请假申请VO（简要信息）
 *
 * @author SIAE Team
 */
@Data
public class LeaveRequestVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 申请人ID
     */
    private Long userId;

    /**
     * 申请人名称
     */
    private String userName;

    /**
     * 申请人头像URL
     */
    private String userAvatarUrl;

    /**
     * 请假类型
     */
    private LeaveType leaveType;

    /**
     * 开始时间
     */
    private LocalDateTime startDate;

    /**
     * 结束时间
     */
    private LocalDateTime endDate;

    /**
     * 请假天数
     */
    private BigDecimal days;

    /**
     * 请假原因
     */
    private String reason;

    /**
     * 状态
     */
    private LeaveStatus status;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 审批人名称
     */
    private String approverName;

    /**
     * 审批人头像URL
     */
    private String approverAvatarUrl;

    /**
     * 审批意见
     */
    private String approvalNote;

    /**
     * 审批时间
     */
    private LocalDateTime approvedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
