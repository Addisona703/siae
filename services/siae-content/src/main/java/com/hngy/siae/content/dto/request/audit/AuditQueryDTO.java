package com.hngy.siae.content.dto.request.audit;

import com.hngy.siae.content.enums.TypeEnum;
import com.hngy.siae.content.enums.status.AuditStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审核查询请求DTO
 *
 * @author KEYKB
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "审核查询请求")
public class AuditQueryDTO {

    @Schema(description = "目标对象ID", example = "1001")
    private Long targetId;

    @Schema(description = "目标类型：CONTENT-内容，COMMENT-评论", example = "CONTENT")
    private TypeEnum targetType;

    @Schema(description = "审核状态：PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝", example = "PENDING")
    private AuditStatusEnum auditStatus;
}
