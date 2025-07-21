package com.hngy.siae.content.dto.response;

import java.util.Date;


import com.hngy.siae.content.common.enums.status.AuditStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditVO {
    private Long id;
    private Long targetId;
    private String targetType;
    private AuditStatusEnum auditStatus;
    private String auditReason;
    private Long auditBy;
    private Date createTime;
}
