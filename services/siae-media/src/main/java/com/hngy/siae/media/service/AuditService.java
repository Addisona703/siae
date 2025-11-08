package com.hngy.siae.media.service;

import com.hngy.siae.media.domain.entity.AuditLog;
import com.hngy.siae.media.domain.enums.ActorType;
import com.hngy.siae.media.domain.enums.AuditAction;
import com.hngy.siae.media.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 审计日志服务
 *
 * @author SIAE Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * 记录审计日志（异步）
     */
    @Async
    public void log(String fileId, String tenantId, ActorType actorType, String actorId,
                    AuditAction action, String ip, String userAgent, Map<String, Object> metadata) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setFileId(fileId);
            auditLog.setTenantId(tenantId);
            auditLog.setActorType(actorType);
            auditLog.setActorId(actorId);
            auditLog.setAction(action);
            auditLog.setIp(ip);
            auditLog.setUserAgent(userAgent);
            auditLog.setMetadata(metadata);
            auditLog.setOccurredAt(LocalDateTime.now());

            auditLogRepository.insert(auditLog);
            log.debug("Audit log recorded: fileId={}, action={}", fileId, action);
        } catch (Exception e) {
            log.error("Failed to record audit log", e);
        }
    }

    /**
     * 记录上传初始化审计
     */
    public void logUploadInit(String fileId, String tenantId, String actorId, Map<String, Object> metadata) {
        log(fileId, tenantId, ActorType.USER, actorId, AuditAction.INIT, null, null, metadata);
    }

    /**
     * 记录上传完成审计
     */
    public void logUploadComplete(String fileId, String tenantId, String actorId, Map<String, Object> metadata) {
        log(fileId, tenantId, ActorType.USER, actorId, AuditAction.COMPLETE, null, null, metadata);
    }

    /**
     * 记录文件更新审计
     */
    public void logFileUpdate(String fileId, String tenantId, String actorId, Map<String, Object> metadata) {
        log(fileId, tenantId, ActorType.USER, actorId, AuditAction.UPDATE_ACL, null, null, metadata);
    }

    /**
     * 记录文件删除审计
     */
    public void logFileDelete(String fileId, String tenantId, String actorId, Map<String, Object> metadata) {
        log(fileId, tenantId, ActorType.USER, actorId, AuditAction.DELETE, null, null, metadata);
    }

    /**
     * 记录文件恢复审计
     */
    public void logFileRestore(String fileId, String tenantId, String actorId, Map<String, Object> metadata) {
        log(fileId, tenantId, ActorType.USER, actorId, AuditAction.RESTORE, null, null, metadata);
    }

    /**
     * 记录文件下载审计
     */
    public void logFileDownload(String fileId, String tenantId, String actorId, String ip, String userAgent, Map<String, Object> metadata) {
        log(fileId, tenantId, ActorType.USER, actorId, AuditAction.DOWNLOAD, ip, userAgent, metadata);
    }

    /**
     * 记录签名生成审计
     */
    public void logSignGenerate(String fileId, String tenantId, String actorId, Map<String, Object> metadata) {
        log(fileId, tenantId, ActorType.USER, actorId, AuditAction.SIGN, null, null, metadata);
    }
}