package com.hngy.siae.media.controller;

import com.hngy.siae.core.result.Result;
import com.hngy.siae.media.security.TenantContext;
import com.hngy.siae.media.service.sign.StreamingService;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.hngy.siae.media.constant.MediaPermissions.*;

/**
 * 流式播放控制器
 * 提供视频、音频的流式播放接口
 *
 * @author SIAE Team
 */
@Slf4j
@RestController
@RequestMapping("/streaming")
@RequiredArgsConstructor
@Tag(name = "流式播放", description = "视频和音频流式播放接口")
public class StreamingController {

    private final StreamingService streamingService;

    /**
     * 生成流式播放URL
     * 支持视频和音频的实时播放，支持拖拽、快进等操作
     */
    @GetMapping("/{fileId}/url")
    @Operation(summary = "生成流式播放URL", description = "生成支持Range请求的流式播放URL")
    @SiaeAuthorize("hasAuthority('" + MEDIA_STREAMING + "')")
    public Result<Map<String, Object>> getStreamingUrl(
            @PathVariable String fileId,
            @RequestParam(defaultValue = "3600") int expirySeconds) {

        String tenantId = TenantContext.getRequiredTenantId();
        String userId = TenantContext.getRequiredUserId();
        log.info("Get streaming URL: fileId={}, tenantId={}, userId={}", fileId, tenantId, userId);

        String url = streamingService.generateStreamingUrl(fileId, tenantId, userId, expirySeconds);
        
        Map<String, Object> response = new HashMap<>();
        response.put("fileId", fileId);
        response.put("url", url);
        response.put("expiresIn", expirySeconds);
        response.put("supportsRange", true); // 支持HTTP Range请求
        
        return Result.success(response);
    }

    /**
     * 获取HLS播放列表（用于自适应流式播放）
     */
    @GetMapping("/{fileId}/hls")
    @Operation(summary = "获取HLS播放列表", description = "获取HLS自适应流式播放的URL列表")
    @SiaeAuthorize("hasAuthority('" + MEDIA_STREAMING + "')")
    public Result<Map<String, String>> getHlsUrls(
            @PathVariable String fileId,
            @RequestParam(defaultValue = "3600") int expirySeconds) {

        String tenantId = TenantContext.getRequiredTenantId();
        String userId = TenantContext.getRequiredUserId();
        log.info("Get HLS URLs: fileId={}, tenantId={}, userId={}", fileId, tenantId, userId);

        Map<String, String> urls = streamingService.generateHlsUrls(fileId, tenantId, userId, expirySeconds);
        return Result.success(urls);
    }

    /**
     * 获取流式播放元数据
     */
    @GetMapping("/{fileId}/metadata")
    @Operation(summary = "获取流式播放元数据", description = "获取媒体文件的元数据信息")
    @SiaeAuthorize("hasAuthority('" + MEDIA_FILE_QUERY + "')")
    public Result<Map<String, Object>> getMetadata(@PathVariable String fileId) {
        String tenantId = TenantContext.getRequiredTenantId();
        log.info("Get streaming metadata: fileId={}, tenantId={}", fileId, tenantId);

        Map<String, Object> metadata = streamingService.getStreamingMetadata(fileId);
        return Result.success(metadata);
    }

}
