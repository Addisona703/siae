package com.hngy.siae.media.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hngy.siae.media.domain.entity.FileEntity;
import com.hngy.siae.media.domain.entity.LifecyclePolicy;
import com.hngy.siae.media.domain.enums.FileStatus;
import com.hngy.siae.media.infrastructure.storage.StorageService;
import com.hngy.siae.media.repository.FileRepository;
import com.hngy.siae.media.repository.LifecyclePolicyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 文件生命周期管理服务
 *
 * @author SIAE Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LifecycleService {

    private final FileRepository fileRepository;
    private final LifecyclePolicyRepository lifecyclePolicyRepository;
    private final StorageService storageService;
    private final QuotaService quotaService;

    /**
     * 执行生命周期策略
     */
    @Transactional
    public void executeLifecyclePolicies() {
        log.info("Starting lifecycle policy execution");

        // 获取所有启用的策略
        LambdaQueryWrapper<LifecyclePolicy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LifecyclePolicy::getEnabled, true);
        List<LifecyclePolicy> policies = lifecyclePolicyRepository.selectList(wrapper);

        for (LifecyclePolicy policy : policies) {
            try {
                executePolicy(policy);
            } catch (Exception e) {
                log.error("Failed to execute lifecycle policy: policyId={}", policy.getId(), e);
            }
        }

        log.info("Lifecycle policy execution completed");
    }

    /**
     * 执行单个策略
     */
    private void executePolicy(LifecyclePolicy policy) {
        log.info("Executing lifecycle policy: policyId={}, name={}", policy.getId(), policy.getName());

        Map<String, Object> rules = policy.getRules();
        
        // 归档规则
        if (rules.containsKey("archiveAfterDays")) {
            int days = getIntFromMap(rules, "archiveAfterDays");
            archiveOldFiles(policy.getTenantId(), days);
        }

        // 删除规则
        if (rules.containsKey("deleteAfterDays")) {
            int days = getIntFromMap(rules, "deleteAfterDays");
            deleteOldFiles(policy.getTenantId(), days);
        }

        // 清理已删除文件
        if (rules.containsKey("cleanupDeletedAfterDays")) {
            int days = getIntFromMap(rules, "cleanupDeletedAfterDays");
            cleanupDeletedFiles(policy.getTenantId(), days);
        }
    }

    /**
     * 归档旧文件（移动到冷存储）
     */
    private void archiveOldFiles(String tenantId, int days) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        
        LambdaQueryWrapper<FileEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileEntity::getTenantId, tenantId)
               .eq(FileEntity::getStatus, FileStatus.COMPLETED)
               .lt(FileEntity::getCreatedAt, threshold)
               .isNull(FileEntity::getDeletedAt);

        List<FileEntity> files = fileRepository.selectList(wrapper);
        
        for (FileEntity file : files) {
            try {
                archiveFile(file);
            } catch (Exception e) {
                log.error("Failed to archive file: fileId={}", file.getId(), e);
            }
        }

        log.info("Archived {} files for tenant: {}", files.size(), tenantId);
    }

    /**
     * 归档单个文件
     */
    private void archiveFile(FileEntity file) {
        // 标记为归档状态
        Map<String, Object> ext = file.getExt();
        if (ext == null) {
            ext = new java.util.HashMap<>();
        }
        ext.put("archived", true);
        ext.put("archivedAt", LocalDateTime.now().toString());
        file.setExt(ext);
        
        fileRepository.updateById(file);
        log.info("Archived file: fileId={}", file.getId());
        
        // TODO: 实际迁移到冷存储（如S3 Glacier）
    }

    /**
     * 删除旧文件（软删除）
     */
    private void deleteOldFiles(String tenantId, int days) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        
        LambdaQueryWrapper<FileEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileEntity::getTenantId, tenantId)
               .lt(FileEntity::getCreatedAt, threshold)
               .isNull(FileEntity::getDeletedAt);

        List<FileEntity> files = fileRepository.selectList(wrapper);
        
        for (FileEntity file : files) {
            try {
                softDeleteFile(file);
            } catch (Exception e) {
                log.error("Failed to delete file: fileId={}", file.getId(), e);
            }
        }

        log.info("Deleted {} files for tenant: {}", files.size(), tenantId);
    }

    /**
     * 软删除文件
     */
    private void softDeleteFile(FileEntity file) {
        file.setDeletedAt(LocalDateTime.now());
        file.setStatus(FileStatus.DELETED);
        fileRepository.updateById(file);
        
        log.info("Soft deleted file: fileId={}", file.getId());
    }

    /**
     * 清理已删除的文件（物理删除）
     */
    private void cleanupDeletedFiles(String tenantId, int days) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        
        LambdaQueryWrapper<FileEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileEntity::getTenantId, tenantId)
               .isNotNull(FileEntity::getDeletedAt)
               .lt(FileEntity::getDeletedAt, threshold);

        List<FileEntity> files = fileRepository.selectList(wrapper);
        
        for (FileEntity file : files) {
            try {
                permanentlyDeleteFile(file);
            } catch (Exception e) {
                log.error("Failed to cleanup file: fileId={}", file.getId(), e);
            }
        }

        log.info("Cleaned up {} files for tenant: {}", files.size(), tenantId);
    }

    /**
     * 永久删除文件
     */
    private void permanentlyDeleteFile(FileEntity file) {
        // 从对象存储中删除
        try {
            storageService.deleteObject(file.getBucket(), file.getStorageKey());
        } catch (Exception e) {
            log.error("Failed to delete object from storage: fileId={}", file.getId(), e);
        }

        // 更新配额
        quotaService.decreaseUsage(file.getTenantId(), file.getSize(), 1);

        // 从数据库中删除
        fileRepository.deleteById(file.getId());
        
        log.info("Permanently deleted file: fileId={}", file.getId());
    }

    private Integer getIntFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.valueOf(value.toString());
    }

}
