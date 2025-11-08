package com.hngy.siae.media.service;

import com.hngy.siae.media.domain.entity.Quota;
import com.hngy.siae.media.repository.QuotaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 配额管理服务
 *
 * @author SIAE Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuotaService {

    private final QuotaRepository quotaRepository;

    /**
     * 获取租户配额信息
     */
    public Quota getQuota(String tenantId) {
        Quota quota = quotaRepository.selectById(tenantId);
        if (quota == null) {
            quota = createDefaultQuota(tenantId);
            quotaRepository.insert(quota);
        }
        return quota;
    }

    /**
     * 检查是否超过存储配额
     */
    public boolean checkStorageQuota(String tenantId, long additionalBytes) {
        Quota quota = getQuota(tenantId);
        Map<String, Object> limits = quota.getLimits();
        Long maxBytes = getLongFromMap(limits, "max_bytes");
        
        long totalUsage = quota.getBytesUsed() + additionalBytes;
        boolean withinLimit = totalUsage <= maxBytes;
        
        if (!withinLimit) {
            log.warn("Storage quota exceeded: tenantId={}, current={}, additional={}, limit={}", 
                    tenantId, quota.getBytesUsed(), additionalBytes, maxBytes);
        }
        
        return withinLimit;
    }

    /**
     * 检查是否超过对象数量配额
     */
    public boolean checkObjectQuota(String tenantId, int additionalObjects) {
        Quota quota = getQuota(tenantId);
        Map<String, Object> limits = quota.getLimits();
        Integer maxObjects = getIntFromMap(limits, "max_objects");
        
        long totalObjects = quota.getObjectsCount() + additionalObjects;
        boolean withinLimit = totalObjects <= maxObjects;
        
        if (!withinLimit) {
            log.warn("Object quota exceeded: tenantId={}, current={}, additional={}, limit={}", 
                    tenantId, quota.getObjectsCount(), additionalObjects, maxObjects);
        }
        
        return withinLimit;
    }

    /**
     * 增加配额使用量
     */
    @Transactional
    public void increaseUsage(String tenantId, long bytes, int objects) {
        Quota quota = getQuota(tenantId);
        quota.setBytesUsed(quota.getBytesUsed() + bytes);
        quota.setObjectsCount(quota.getObjectsCount() + objects);
        quotaRepository.updateById(quota);
        
        log.info("Increased quota usage: tenantId={}, bytes={}, objects={}", tenantId, bytes, objects);
        
        // 检查是否接近配额限制
        checkQuotaWarning(quota);
    }

    /**
     * 减少配额使用量
     */
    @Transactional
    public void decreaseUsage(String tenantId, long bytes, int objects) {
        Quota quota = getQuota(tenantId);
        quota.setBytesUsed(Math.max(0, quota.getBytesUsed() - bytes));
        quota.setObjectsCount(Math.max(0, quota.getObjectsCount() - objects));
        quotaRepository.updateById(quota);
        
        log.info("Decreased quota usage: tenantId={}, bytes={}, objects={}", tenantId, bytes, objects);
    }

    /**
     * 更新租户配额限制
     */
    @Transactional
    public void updateQuotaLimits(String tenantId, Long maxBytes, Integer maxObjects) {
        Quota quota = getQuota(tenantId);
        Map<String, Object> limits = quota.getLimits();
        
        if (maxBytes != null) {
            limits.put("max_bytes", maxBytes);
        }
        if (maxObjects != null) {
            limits.put("max_objects", maxObjects);
        }
        
        quota.setLimits(limits);
        quotaRepository.updateById(quota);
        
        log.info("Updated quota limits: tenantId={}, maxBytes={}, maxObjects={}", 
                tenantId, maxBytes, maxObjects);
    }

    /**
     * 获取配额使用率
     */
    public Map<String, Object> getQuotaUsage(String tenantId) {
        Quota quota = getQuota(tenantId);
        Map<String, Object> limits = quota.getLimits();
        
        Long maxBytes = getLongFromMap(limits, "max_bytes");
        Integer maxObjects = getIntFromMap(limits, "max_objects");
        
        double bytesUsagePercent = (double) quota.getBytesUsed() / maxBytes * 100;
        double objectsUsagePercent = (double) quota.getObjectsCount() / maxObjects * 100;
        
        Map<String, Object> usage = new HashMap<>();
        usage.put("tenantId", tenantId);
        usage.put("bytesUsed", quota.getBytesUsed());
        usage.put("bytesLimit", maxBytes);
        usage.put("bytesUsagePercent", Math.round(bytesUsagePercent * 100) / 100.0);
        usage.put("objectsUsed", quota.getObjectsCount());
        usage.put("objectsLimit", maxObjects);
        usage.put("objectsUsagePercent", Math.round(objectsUsagePercent * 100) / 100.0);
        
        return usage;
    }

    /**
     * 重置配额使用量（用于月度重置）
     */
    @Transactional
    public void resetQuota(String tenantId) {
        Quota quota = getQuota(tenantId);
        
        if ("monthly".equals(quota.getResetStrategy())) {
            quota.setBytesUsed(0L);
            quota.setObjectsCount(0L);
            quotaRepository.updateById(quota);
            
            log.info("Reset quota: tenantId={}", tenantId);
        }
    }

    /**
     * 检查配额告警
     */
    private void checkQuotaWarning(Quota quota) {
        Map<String, Object> limits = quota.getLimits();
        Long maxBytes = getLongFromMap(limits, "max_bytes");
        Integer maxObjects = getIntFromMap(limits, "max_objects");
        
        double bytesUsagePercent = (double) quota.getBytesUsed() / maxBytes * 100;
        double objectsUsagePercent = (double) quota.getObjectsCount() / maxObjects * 100;
        
        // 80%告警
        if (bytesUsagePercent >= 80 || objectsUsagePercent >= 80) {
            log.warn("Quota warning: tenantId={}, bytesUsage={}%, objectsUsage={}%", 
                    quota.getTenantId(), 
                    Math.round(bytesUsagePercent), 
                    Math.round(objectsUsagePercent));
            // TODO: 发送告警通知
        }
        
        // 90%严重告警
        if (bytesUsagePercent >= 90 || objectsUsagePercent >= 90) {
            log.error("Quota critical warning: tenantId={}, bytesUsage={}%, objectsUsage={}%", 
                    quota.getTenantId(), 
                    Math.round(bytesUsagePercent), 
                    Math.round(objectsUsagePercent));
            // TODO: 发送严重告警通知
        }
    }

    /**
     * 创建默认配额
     */
    private Quota createDefaultQuota(String tenantId) {
        Quota quota = new Quota();
        quota.setTenantId(tenantId);
        quota.setBytesUsed(0L);
        quota.setObjectsCount(0L);
        
        Map<String, Object> limits = new HashMap<>();
        limits.put("max_bytes", 10737418240L); // 10GB
        limits.put("max_objects", 10000);
        limits.put("daily_download", "unlimited");
        quota.setLimits(limits);
        
        quota.setResetStrategy("monthly");
        return quota;
    }

    private Long getLongFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.valueOf(value.toString());
    }

    private Integer getIntFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.valueOf(value.toString());
    }

}
