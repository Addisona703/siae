package com.hngy.siae.content.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.hngy.siae.content.enums.TypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核历史记录实体
 * 用于记录每次审核操作的完整信息，支持审核追溯
 * <p>
 * &#064;TableName  audit_log
 * @author Kiro
 */
@Data
@TableName("audit_log")
public class AuditLog {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 目标ID（内容ID或评论ID）
     */
    private Long targetId;

    /**
     * 目标类型：0-内容，1-评论
     */
    private TypeEnum targetType;

    /**
     * 审核前状态
     */
    private Integer fromStatus;

    /**
     * 审核后状态：0-待审核，1-通过，2-已删除
     */
    private Integer toStatus;

    /**
     * 审核原因/备注
     */
    private String auditReason;

    /**
     * 审核人ID
     */
    private Long auditBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
