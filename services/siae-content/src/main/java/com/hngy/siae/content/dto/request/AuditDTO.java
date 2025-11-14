package com.hngy.siae.content.dto.request;


import com.hngy.siae.content.enums.TypeEnum;
import com.hngy.siae.content.enums.status.AuditStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditDTO {
    @NotNull
    private Long targetId;
    private TypeEnum targetType;
    @NotNull
    private AuditStatusEnum auditStatus;
    private String auditReason;
    @NotNull
    private Long auditBy;
    private Integer version;
}
