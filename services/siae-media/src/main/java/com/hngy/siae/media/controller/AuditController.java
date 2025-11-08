package com.hngy.siae.media.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.media.domain.entity.AuditLog;
import com.hngy.siae.media.domain.enums.AuditAction;
import com.hngy.siae.media.repository.AuditLogRepository;
import com.hngy.siae.media.security.TenantContext;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static com.hngy.siae.media.constant.MediaPermissions.*;

/**
 * 审计日志控制器
 *
 * @author SIAE Team
 */
@Slf4j
@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
@Tag(name = "审计日志", description = "审计日志查询和统计接口")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    /**
     * 查询审计日志
     */
    @GetMapping
    @Operation(summary = "查询审计日志", description = "分页查询审计日志，支持多条件筛选")
    @SiaeAuthorize("hasAuthority('" + MEDIA_AUDIT_QUERY + "')")
    public Result<IPage<AuditLog>> queryAuditLogs(
            @RequestParam(required = false) String fileId,
            @RequestParam(required = false) String actorId,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        String tenantId = TenantContext.getRequiredTenantId();
        log.info("Query audit logs: tenantId={}, fileId={}, action={}", tenantId, fileId, action);

        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLog::getTenantId, tenantId);

        if (fileId != null) {
            wrapper.eq(AuditLog::getFileId, fileId);
        }
        if (actorId != null) {
            wrapper.eq(AuditLog::getActorId, actorId);
        }
        if (action != null) {
            wrapper.eq(AuditLog::getAction, action);
        }
        if (startTime != null) {
            wrapper.ge(AuditLog::getOccurredAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(AuditLog::getOccurredAt, endTime);
        }

        wrapper.orderByDesc(AuditLog::getOccurredAt);

        Page<AuditLog> pageRequest = new Page<>(page, size);
        IPage<AuditLog> result = auditLogRepository.selectPage(pageRequest, wrapper);

        // 脱敏处理
        result.getRecords().forEach(this::maskSensitiveData);

        return Result.success(result);
    }

    /**
     * 获取审计日志详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取审计日志详情", description = "根据ID获取审计日志详细信息")
    @SiaeAuthorize("hasAuthority('" + MEDIA_AUDIT_QUERY + "')")
    public Result<AuditLog> getAuditLog(@PathVariable Long id) {
        String tenantId = TenantContext.getRequiredTenantId();
        log.info("Get audit log: id={}, tenantId={}", id, tenantId);

        AuditLog auditLog = auditLogRepository.selectById(id);
        if (auditLog == null || !auditLog.getTenantId().equals(tenantId)) {
            return Result.error("审计日志不存在");
        }

        maskSensitiveData(auditLog);
        return Result.success(auditLog);
    }

    /**
     * 导出审计日志
     */
    @GetMapping("/export")
    @Operation(summary = "导出审计日志", description = "导出指定时间范围的审计日志")
    @SiaeAuthorize("hasAuthority('" + MEDIA_AUDIT_EXPORT + "')")
    public Result<List<AuditLog>> exportAuditLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        String tenantId = TenantContext.getRequiredTenantId();
        log.info("Export audit logs: tenantId={}, startTime={}, endTime={}", tenantId, startTime, endTime);

        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLog::getTenantId, tenantId);

        if (startTime != null) {
            wrapper.ge(AuditLog::getOccurredAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(AuditLog::getOccurredAt, endTime);
        }

        wrapper.orderByDesc(AuditLog::getOccurredAt);
        wrapper.last("LIMIT 10000"); // 限制导出数量

        List<AuditLog> logs = auditLogRepository.selectList(wrapper);
        logs.forEach(this::maskSensitiveData);

        return Result.success(logs);
    }

    /**
     * 获取审计统计
     */
    @GetMapping("/stats")
    @Operation(summary = "获取审计统计", description = "获取审计日志的统计信息")
    @SiaeAuthorize("hasAuthority('" + MEDIA_AUDIT_QUERY + "')")
    public Result<AuditStats> getAuditStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        String tenantId = TenantContext.getRequiredTenantId();
        log.info("Get audit stats: tenantId={}", tenantId);

        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLog::getTenantId, tenantId);

        if (startTime != null) {
            wrapper.ge(AuditLog::getOccurredAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(AuditLog::getOccurredAt, endTime);
        }

        Long totalCount = auditLogRepository.selectCount(wrapper);

        AuditStats stats = new AuditStats();
        stats.setTotalCount(totalCount);
        stats.setTenantId(tenantId);
        stats.setStartTime(startTime);
        stats.setEndTime(endTime);

        // 按操作类型统计
        for (AuditAction action : AuditAction.values()) {
            LambdaQueryWrapper<AuditLog> actionWrapper = new LambdaQueryWrapper<>();
            actionWrapper.eq(AuditLog::getTenantId, tenantId)
                        .eq(AuditLog::getAction, action);
            if (startTime != null) {
                actionWrapper.ge(AuditLog::getOccurredAt, startTime);
            }
            if (endTime != null) {
                actionWrapper.le(AuditLog::getOccurredAt, endTime);
            }
            Long count = auditLogRepository.selectCount(actionWrapper);
            stats.getActionCounts().put(action.getValue(), count);
        }

        return Result.success(stats);
    }

    /**
     * 脱敏敏感数据
     */
    private void maskSensitiveData(AuditLog log) {
        // 脱敏IP地址（保留前两段）
        if (log.getIp() != null) {
            String[] parts = log.getIp().split("\\.");
            if (parts.length == 4) {
                log.setIp(parts[0] + "." + parts[1] + ".***.**");
            }
        }

        // 脱敏User-Agent（只保留浏览器类型）
        if (log.getUserAgent() != null && log.getUserAgent().length() > 50) {
            log.setUserAgent(log.getUserAgent().substring(0, 50) + "...");
        }
    }

    @Data
    public static class AuditStats {
        private String tenantId;
        private Long totalCount;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private java.util.Map<String, Long> actionCounts = new java.util.HashMap<>();
    }

}
