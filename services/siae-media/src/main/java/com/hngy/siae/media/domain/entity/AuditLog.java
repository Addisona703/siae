package com.hngy.siae.media.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.hngy.siae.media.domain.enums.ActorType;
import com.hngy.siae.media.domain.enums.AuditAction;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 审计日志实体
 *
 * @author SIAE Team
 */
@Data
@TableName(value = "audit_logs", autoResultMap = true)
public class AuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String fileId;

    private String tenantId;

    private ActorType actorType;

    private String actorId;

    private AuditAction action;

    private String ip;

    private String userAgent;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> metadata;

    private LocalDateTime occurredAt;

}
