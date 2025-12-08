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
 * 映射到 audit_logs 表，记录所有文件操作的审计日志
 * 
 * 用于记录文件相关的所有操作，包括：
 * - 初始化上传（init）
 * - 完成上传（complete）
 * - 下载文件（download）
 * - 删除文件（delete）
 * - 更新访问策略（update_policy）
 * 
 * 字段说明：
 * - id: 日志自增ID
 * - fileId: 关联的文件ID（可选，某些操作可能不关联具体文件）
 * - tenantId: 租户ID，用于多租户隔离
 * - actorType: 操作者类型（service/user/system）
 * - actorId: 操作者ID
 * - action: 操作类型
 * - ip: 操作者IP地址
 * - userAgent: 用户代理字符串
 * - metadata: 操作相关的元数据（JSON格式）
 * - occurredAt: 操作发生时间
 *
 * @author SIAE Team
 */
@Data
@TableName(value = "audit_logs", autoResultMap = true)
public class AuditLog {

    /**
     * 日志ID
     * 自增主键
     */
    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Long id;

    /**
     * 文件ID
     * 关联的文件ID（可选）
     * 某些操作可能不关联具体文件
     */
    @TableField("file_id")
    private String fileId;

    /**
     * 租户ID
     * 用于多租户隔离
     */
    @TableField("tenant_id")
    private String tenantId;

    /**
     * 操作者类型
     * service: 服务调用
     * user: 用户操作
     * system: 系统操作
     */
    @TableField("actor_type")
    private ActorType actorType;

    /**
     * 操作者ID
     * 根据 actorType 不同，可能是服务名、用户ID或系统标识
     */
    @TableField("actor_id")
    private String actorId;

    /**
     * 操作类型
     * init: 初始化上传
     * complete: 完成上传
     * download: 下载文件
     * delete: 删除文件
     * update_policy: 更新访问策略
     */
    @TableField("action")
    private AuditAction action;

    /**
     * IP地址
     * 操作者的IP地址（IPv4或IPv6）
     * 最大长度45字符（支持IPv6）
     */
    @TableField("ip")
    private String ip;

    /**
     * 用户代理
     * HTTP User-Agent字符串
     * 用于记录客户端信息
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 元数据
     * JSON对象，存储操作相关的额外信息
     * 例如：文件大小、访问策略变更前后值等
     */
    @TableField(value = "metadata", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> metadata;

    /**
     * 发生时间
     * 操作发生的时间戳（毫秒精度）
     */
    @TableField("occurred_at")
    private LocalDateTime occurredAt;

}
