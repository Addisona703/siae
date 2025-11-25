package com.hngy.siae.attendance.dto.request;

import com.hngy.siae.attendance.enums.LeaveType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 创建请假申请DTO
 *
 * @author SIAE Team
 */
@Data
public class LeaveRequestCreateDTO {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 请假类型
     */
    @NotNull(message = "请假类型不能为空")
    private LeaveType leaveType;

    /**
     * 开始日期
     */
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    /**
     * 请假原因
     */
    @NotBlank(message = "请假原因不能为空")
    private String reason;

    /**
     * 附件文件ID列表
     */
    private List<String> attachmentFileIds;
}
