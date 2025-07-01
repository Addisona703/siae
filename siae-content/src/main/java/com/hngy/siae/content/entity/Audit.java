package com.hngy.siae.content.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;
import com.hngy.siae.content.common.enums.TypeEnum;
import com.hngy.siae.content.common.enums.status.AuditStatusEnum;
import lombok.Data;

/**
 * 审核记录表
 * @TableName content_audit
 */
@Data
public class Audit {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 被审核对象的主键ID
     */
    private Long targetId;

    /**
     * 审核对象类型（如 content/comment）
     */
    private TypeEnum targetType;

    /**
     * 审核状态（0待审核、1通过、2不通过）
     */
    private AuditStatusEnum auditStatus;

    /**
     * 审核意见
     */
    private String auditReason;

    /**
     * 审核人用户ID
     */
    private Long auditBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}