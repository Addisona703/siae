package com.hngy.siae.media.service.impl;

import com.hngy.siae.media.domain.entity.AuditLog;
import com.hngy.siae.media.domain.enums.AccessPolicy;
import com.hngy.siae.media.domain.enums.ActorType;
import com.hngy.siae.media.domain.enums.AuditAction;
import com.hngy.siae.media.mapper.AuditLogMapper;
import com.hngy.siae.media.service.IAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 审计日志服务实现
 * 
 * 实现审计日志记录功能，所有方法都是异步执行
 * 使用 @Async 注解确保审计日志记录不阻塞主业务流程
 * 
 * 审计日志记录失败不影响业务操作，只记录错误日志
 * 
 * Requirements: 2.1
 *
 * @author SIAE Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements IAuditService {

    private final AuditLogMapper auditLogMapper;

    /**
     * 记录审计日志（异步）
     * 
     * 通用的审计日志记录方法，所有其他方法都调用此方法
     * 使用 @Async 注解实现异步执行，不阻塞主业务流程
     * 
     * 如果记录失败，捕获异常并记录错误日志，不影响业务
     * 
     * @param fileId 文件ID
     * @param tenantId 租户ID
     * @param actorType 操作者类型
     * @param actorId 操作者ID
     * @param action 操作类型
     * @param ip IP地址
     * @param userAgent 用户代理
     * @param metadata 元数据
     * 
     * Requirements: 2.1
     */
    @Async
    @Override
    public void log(String fileId, String tenantId, ActorType actorType, String actorId,
                    AuditAction action, String ip, String userAgent, Map<String, Object> metadata) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setFileId(fileId);
            auditLog.setTenantId(tenantId);
            auditLog.setActorType(actorType);
            // 如果 actorId 为空，使用 "system" 作为默认值，避免数据库约束错误
            auditLog.setActorId(actorId != null ? actorId : "system");
            auditLog.setAction(action);
            auditLog.setIp(ip);
            auditLog.setUserAgent(userAgent);
            auditLog.setMetadata(metadata);
            auditLog.setOccurredAt(LocalDateTime.now());

            auditLogMapper.insert(auditLog);
            
            log.debug("Audit log recorded: fileId={}, tenantId={}, action={}, actorId={}", 
                     fileId, tenantId, action, actorId);
        } catch (Exception e) {
            // 审计日志记录失败不影响业务，只记录错误日志
            log.error("Failed to record audit log: fileId={}, tenantId={}, action={}, actorId={}", 
                     fileId, tenantId, action, actorId, e);
        }
    }

    /**
     * 记录上传初始化审计
     * 
     * 记录文件上传初始化操作
     * 元数据包含：文件名、文件大小、MIME类型、访问策略等
     * 
     * @param fileId 文件ID
     * @param tenantId 租户ID
     * @param actorId 操作者ID
     * @param metadata 元数据（文件信息）
     * 
     * Requirements: 2.1
     */
    @Override
    public void logUploadInit(String fileId, String tenantId, String actorId, Map<String, Object> metadata) {
        log(fileId, tenantId, ActorType.USER, actorId, AuditAction.INIT, null, null, metadata);
    }

    /**
     * 记录上传完成审计
     * 
     * 记录文件上传完成操作
     * 元数据包含：文件ID、存储路径、文件大小等
     * 
     * @param fileId 文件ID
     * @param tenantId 租户ID
     * @param actorId 操作者ID
     * @param metadata 元数据（上传结果信息）
     * 
     * Requirements: 2.1
     */
    @Override
    public void logUploadComplete(String fileId, String tenantId, String actorId, Map<String, Object> metadata) {
        log(fileId, tenantId, ActorType.USER, actorId, AuditAction.COMPLETE, null, null, metadata);
    }

    /**
     * 记录文件删除审计
     * 
     * 记录文件删除操作
     * 元数据包含：文件名、文件大小、删除原因等
     * 
     * @param fileId 文件ID
     * @param tenantId 租户ID
     * @param actorId 操作者ID
     * @param metadata 元数据（删除信息）
     * 
     * Requirements: 2.1
     */
    @Override
    public void logFileDelete(String fileId, String tenantId, String actorId, Map<String, Object> metadata) {
        log(fileId, tenantId, ActorType.USER, actorId, AuditAction.DELETE, null, null, metadata);
    }

    /**
     * 记录访问策略更新审计
     * 
     * 记录文件访问策略变更操作（PUBLIC <-> PRIVATE）
     * 自动构建元数据，包含旧策略和新策略
     * 
     * @param fileId 文件ID
     * @param tenantId 租户ID
     * @param actorId 操作者ID
     * @param oldPolicy 旧的访问策略
     * @param newPolicy 新的访问策略
     * 
     * Requirements: 2.1
     */
    @Override
    public void logAccessPolicyUpdate(String fileId, String tenantId, String actorId, 
                                      AccessPolicy oldPolicy, AccessPolicy newPolicy) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("oldPolicy", oldPolicy != null ? oldPolicy.getValue() : null);
        metadata.put("newPolicy", newPolicy != null ? newPolicy.getValue() : null);
        metadata.put("policyChange", String.format("%s -> %s", 
                                                   oldPolicy != null ? oldPolicy.getValue() : "null", 
                                                   newPolicy != null ? newPolicy.getValue() : "null"));
        
        log(fileId, tenantId, ActorType.USER, actorId, AuditAction.UPDATE_POLICY, null, null, metadata);
    }

    /**
     * 记录文件下载审计
     * 
     * 记录文件下载操作
     * 包含IP地址和用户代理信息，用于安全审计
     * 
     * @param fileId 文件ID
     * @param tenantId 租户ID
     * @param actorId 操作者ID
     * @param ip IP地址
     * @param userAgent 用户代理
     * @param metadata 元数据（下载信息）
     * 
     * Requirements: 2.1
     */
    @Override
    public void logFileDownload(String fileId, String tenantId, String actorId, 
                               String ip, String userAgent, Map<String, Object> metadata) {
        log(fileId, tenantId, ActorType.USER, actorId, AuditAction.DOWNLOAD, ip, userAgent, metadata);
    }

    /**
     * 记录文件更新审计
     * 
     * 记录文件元数据更新操作
     * 元数据包含：变更的字段、旧值、新值等
     * 
     * @param fileId 文件ID
     * @param tenantId 租户ID
     * @param actorId 操作者ID
     * @param metadata 元数据（更新信息）
     * 
     * Requirements: 2.1
     */
    @Override
    public void logFileUpdate(String fileId, String tenantId, String actorId, Map<String, Object> metadata) {
        log(fileId, tenantId, ActorType.USER, actorId, AuditAction.UPDATE_ACL, null, null, metadata);
    }

    /**
     * 记录文件恢复审计
     * 
     * 记录文件恢复操作（从已删除状态恢复）
     * 元数据包含：文件名、文件大小、恢复原因等
     * 
     * @param fileId 文件ID
     * @param tenantId 租户ID
     * @param actorId 操作者ID
     * @param metadata 元数据（恢复信息）
     * 
     * Requirements: 2.1
     */
    @Override
    public void logFileRestore(String fileId, String tenantId, String actorId, Map<String, Object> metadata) {
        log(fileId, tenantId, ActorType.USER, actorId, AuditAction.RESTORE, null, null, metadata);
    }
}
