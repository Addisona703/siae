package com.hngy.siae.attendance.dto.request;

import com.hngy.siae.attendance.enums.LeaveType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 更新请假申请DTO
 *
 * @author SIAE Team
 */
@Data
public class LeaveRequestUpdateDTO {

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
     * 请假原因
     */
    private String reason;

    /**
     * 附件文件ID列表
     */
    private List<String> attachmentFileIds;
}
