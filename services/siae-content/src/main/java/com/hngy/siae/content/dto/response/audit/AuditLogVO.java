package com.hngy.siae.content.dto.response.audit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审核历史记录响应 VO
 *
 * @author KEYKB
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "审核历史记录响应对象")
public class AuditLogVO {

    @Schema(description = "记录ID", example = "1")
    private Long id;

    @Schema(description = "目标ID", example = "1001")
    private Long targetId;

    @Schema(description = "目标类型：1-内容，2-评论", example = "1")
    private Integer targetType;

    @Schema(description = "审核前状态", example = "0")
    private Integer fromStatus;

    @Schema(description = "审核后状态", example = "2")
    private Integer toStatus;

    @Schema(description = "审核原因", example = "内容符合社区规范")
    private String auditReason;

    @Schema(description = "审核人ID", example = "10001")
    private Long auditBy;

    @Schema(description = "审核人名称", example = "管理员张三")
    private String auditByName;

    @Schema(description = "创建时间", example = "2025-11-27T10:30:00")
    private LocalDateTime createTime;
}
