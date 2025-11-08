package com.hngy.siae.media.controller;

import com.hngy.siae.core.result.Result;
import com.hngy.siae.media.domain.dto.sign.SignRequest;
import com.hngy.siae.media.domain.dto.sign.SignResponse;
import com.hngy.siae.media.security.TenantContext;
import com.hngy.siae.media.service.sign.SignService;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import com.hngy.siae.web.utils.WebUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static com.hngy.siae.media.constant.MediaPermissions.*;

/**
 * 下载控制器
 * 提供文件下载相关接口
 *
 * @author SIAE Team
 */
@Slf4j
@RestController
@RequestMapping("/download")
@RequiredArgsConstructor
@Tag(name = "下载管理", description = "文件下载签名生成和验证接口")
public class DownloadController {

    private final SignService signService;

    /**
     * 生成下载签名
     * <p>
     * 功能说明：
     * - 生成临时的预签名下载URL
     * - 支持IP绑定，防止链接被盗用
     * - 支持单次使用，下载后自动失效
     * - 支持自定义过期时间
     */
    @PostMapping("/sign")
    @Operation(summary = "生成下载签名", description = "生成文件下载的临时签名URL，支持IP绑定和单次使用")
    @SiaeAuthorize("hasAuthority('" + MEDIA_DOWNLOAD + "')")
    public Result<SignResponse> generateSign(
            @Valid @RequestBody SignRequest request,
            HttpServletRequest httpRequest) {
        
        String tenantId = TenantContext.getRequiredTenantId();
        String userId = TenantContext.getRequiredUserId();
        String ip = WebUtils.getClientIp(httpRequest);
        log.info("Generate download sign request: fileId={}, tenantId={}, userId={}, ip={}", 
                request.getFileId(), tenantId, userId, ip);

        SignResponse response = signService.generateDownloadSign(request, tenantId, userId, ip);
        return Result.success(response);
    }

    /**
     * 批量生成下载签名
     * <p>
     * 适用场景：
     * - 批量下载多个文件
     * - 打包下载
     */
    @PostMapping("/sign/batch")
    @Operation(summary = "批量生成下载签名", description = "为多个文件批量生成下载签名")
    @SiaeAuthorize("hasAuthority('" + MEDIA_DOWNLOAD + "')")
    public Result<java.util.List<SignResponse>> generateBatchSign(
            @Valid @RequestBody java.util.List<SignRequest> requests,
            HttpServletRequest httpRequest) {
        
        String tenantId = TenantContext.getRequiredTenantId();
        String userId = TenantContext.getRequiredUserId();
        String ip = WebUtils.getClientIp(httpRequest);
        log.info("Generate batch download sign request: count={}, tenantId={}, userId={}", 
                requests.size(), tenantId, userId);

        java.util.List<SignResponse> responses = new java.util.ArrayList<>();
        for (SignRequest request : requests) {
            try {
                SignResponse response = signService.generateDownloadSign(request, tenantId, userId, ip);
                responses.add(response);
            } catch (Exception e) {
                log.error("Failed to generate sign for file: {}", request.getFileId(), e);
                // 继续处理其他文件
            }
        }

        return Result.success(responses);
    }

    /**
     * 验证下载令牌
     * <p>
     * 用于在实际下载前验证令牌的有效性
     */
    @GetMapping("/verify")
    @Operation(summary = "验证下载令牌", description = "验证下载令牌是否有效")
    @SiaeAuthorize("hasAuthority('" + MEDIA_DOWNLOAD + "')")
    public Result<java.util.Map<String, Object>> verifyToken(
            @RequestParam String token,
            @RequestParam String fileId,
            HttpServletRequest httpRequest) {
        
        String ip = WebUtils.getClientIp(httpRequest);
        log.info("Verify download token: fileId={}, token={}, ip={}", fileId, token, ip);

        boolean valid = signService.validateDownloadToken(token, fileId, ip);
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("valid", valid);
        result.put("fileId", fileId);
        result.put("token", token);
        
        return Result.success(result);
    }

    /**
     * 获取文件下载信息
     * <p>
     * 返回文件的基本信息，用于下载前的预览
     */
    @GetMapping("/{fileId}/info")
    @Operation(summary = "获取文件下载信息", description = "获取文件的基本信息，用于下载前预览")
    @SiaeAuthorize("hasAuthority('" + MEDIA_FILE_QUERY + "')")
    public Result<java.util.Map<String, Object>> getDownloadInfo(@PathVariable String fileId) {
        String tenantId = TenantContext.getRequiredTenantId();
        log.info("Get download info: fileId={}, tenantId={}", fileId, tenantId);

        // TODO: 实现获取文件下载信息的逻辑
        // 可以返回文件名、大小、类型等信息
        
        java.util.Map<String, Object> info = new java.util.HashMap<>();
        info.put("fileId", fileId);
        info.put("message", "功能开发中");
        
        return Result.success(info);
    }

}
