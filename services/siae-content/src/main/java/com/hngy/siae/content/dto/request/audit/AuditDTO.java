package com.hngy.siae.content.dto.request.audit;

import com.hngy.siae.content.enums.TypeEnum;
import com.hngy.siae.content.enums.status.AuditStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审核操作请求DTO
 *
 * @author KEYKB
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "审核操作请求")
public class AuditDTO {

    @NotNull(message = "目标ID不能为空")
    @Schema(description = "审核目标ID", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long targetId;

    @NotNull(message = "目标类型不能为空")
    @Schema(description = "目标类型：CONTENT-内容，COMMENT-评论", example = "CONTENT", requiredMode = Schema.RequiredMode.REQUIRED)
    private TypeEnum targetType;

    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核状态：APPROVED-通过，REJECTED-拒绝", example = "APPROVED", requiredMode = Schema.RequiredMode.REQUIRED)
    private AuditStatusEnum auditStatus;

    @Schema(description = "审核原因/备注", example = "内容符合社区规范，审核通过")
    private String auditReason;

    @Schema(description = "审核人ID（由后端自动填充）", example = "10001")
    private Long auditBy;

    @Schema(description = "乐观锁版本号，用于并发控制", example = "1")
    private Integer version;
}
