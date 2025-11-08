package com.hngy.siae.media.controller;

import com.hngy.siae.core.result.Result;
import com.hngy.siae.media.domain.entity.Quota;
import com.hngy.siae.media.security.TenantContext;
import com.hngy.siae.media.service.QuotaService;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.hngy.siae.media.constant.MediaPermissions.*;

/**
 * 配额管理控制器
 *
 * @author SIAE Team
 */
@Slf4j
@RestController
@RequestMapping("/quota")
@RequiredArgsConstructor
@Tag(name = "配额管理", description = "租户配额查询和管理接口")
public class QuotaController {

    private final QuotaService quotaService;

    /**
     * 获取租户配额信息
     */
    @GetMapping
    @Operation(summary = "获取配额信息", description = "获取当前租户的配额信息")
    @SiaeAuthorize("hasAuthority('" + MEDIA_QUOTA_QUERY + "')")
    public Result<Quota> getQuota() {
        String tenantId = TenantContext.getRequiredTenantId();
        log.info("Get quota: tenantId={}", tenantId);
        Quota quota = quotaService.getQuota(tenantId);
        return Result.success(quota);
    }

    /**
     * 获取配额使用情况
     */
    @GetMapping("/usage")
    @Operation(summary = "获取配额使用情况", description = "获取当前租户的配额使用详情")
    @SiaeAuthorize("hasAuthority('" + MEDIA_QUOTA_QUERY + "')")
    public Result<Map<String, Object>> getQuotaUsage() {
        String tenantId = TenantContext.getRequiredTenantId();
        log.info("Get quota usage: tenantId={}", tenantId);
        Map<String, Object> usage = quotaService.getQuotaUsage(tenantId);
        return Result.success(usage);
    }

    /**
     * 更新配额限制（管理员接口）
     */
    @PutMapping("/limits")
    @Operation(summary = "更新配额限制", description = "更新租户的配额限制（管理员接口）")
    @SiaeAuthorize("hasAuthority('" + MEDIA_QUOTA_MANAGE + "')")
    public Result<Void> updateQuotaLimits(@RequestBody QuotaLimitsRequest request) {
        String tenantId = TenantContext.getRequiredTenantId();
        log.info("Update quota limits: tenantId={}, maxBytes={}, maxObjects={}", 
                tenantId, request.getMaxBytes(), request.getMaxObjects());
        
        quotaService.updateQuotaLimits(tenantId, request.getMaxBytes(), request.getMaxObjects());
        return Result.success();
    }

    @Data
    public static class QuotaLimitsRequest {
        private Long maxBytes;
        private Integer maxObjects;
    }

}
