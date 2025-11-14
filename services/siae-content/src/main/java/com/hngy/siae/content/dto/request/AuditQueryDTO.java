package com.hngy.siae.content.dto.request;

import com.hngy.siae.content.enums.TypeEnum;
import com.hngy.siae.content.enums.status.AuditStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审核查询请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "审核查询请求对象")
public class AuditQueryDTO {

    @Schema(description = "目标对象ID")
    private Long targetId;

    @Schema(description = "目标类型（CONTENT、COMMENT等）")
    private TypeEnum targetType;

    @Schema(description = "审核状态（PENDING、APPROVED、REJECTED）")
    private AuditStatusEnum auditStatus;
}
