package com.hngy.siae.attendance.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报表VO
 *
 * @author SIAE Team
 */
@Data
public class ReportVO {

    /**
     * 报表ID
     */
    private String reportId;

    /**
     * 报表类型
     */
    private String reportType;

    /**
     * 报表名称
     */
    private String reportName;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 文件格式
     */
    private String format;

    /**
     * 生成时间
     */
    private LocalDateTime generatedAt;

    /**
     * 生成人ID
     */
    private Long generatedBy;

    /**
     * 生成人名称
     */
    private String generatorName;
}
