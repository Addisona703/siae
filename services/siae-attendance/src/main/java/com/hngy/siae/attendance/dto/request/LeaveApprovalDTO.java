package com.hngy.siae.attendance.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 请假审批DTO
 *
 * @author SIAE Team
 */
@Data
public class LeaveApprovalDTO {

    /**
     * 是否批准
     */
    @NotNull(message = "审批结果不能为空")
    private Boolean approved;

    /**
     * 审批意见
     */
    private String reason;
}
