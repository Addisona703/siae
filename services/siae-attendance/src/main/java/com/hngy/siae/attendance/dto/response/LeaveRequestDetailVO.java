package com.hngy.siae.attendance.dto.response;

import com.hngy.siae.attendance.enums.LeaveStatus;
import com.hngy.siae.attendance.enums.LeaveType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 请假申请详细信息VO
 *
 * @author SIAE Team
 */
@Data
public class LeaveRequestDetailVO {

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
     * 请假类型
     */
    private LeaveType leaveType;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

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
     * 审批意见
     */
    private String approvalNote;

    /**
     * 审批时间
     */
    private LocalDateTime approvedAt;

    /**
     * 附件文件ID列表
     */
    private List<String> attachmentFileIds;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
