package com.hngy.siae.attendance.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.hngy.siae.attendance.enums.LeaveStatus;
import com.hngy.siae.attendance.enums.LeaveType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 请假申请实体
 *
 * @author SIAE Team
 */
@Data
@TableName(value = "leave_request", autoResultMap = true)
public class LeaveRequest {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 申请人ID
     */
    private Long userId;

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
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> attachmentFileIds;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    private Integer deleted;
}
