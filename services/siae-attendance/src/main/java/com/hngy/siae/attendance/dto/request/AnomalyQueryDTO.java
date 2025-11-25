package com.hngy.siae.attendance.dto.request;

import com.hngy.siae.attendance.enums.AnomalyType;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤异常查询条件DTO
 *
 * @author SIAE Team
 */
@Data
public class AnomalyQueryDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户ID列表
     */
    private List<Long> userIds;

    /**
     * 异常类型
     */
    private AnomalyType anomalyType;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 是否已处理
     */
    private Boolean resolved;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 关键字搜索
     */
    private String keyword;
}
