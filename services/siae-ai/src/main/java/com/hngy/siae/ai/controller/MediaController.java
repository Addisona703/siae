package com.hngy.siae.ai.controller;

import com.hngy.siae.ai.domain.dto.ImageGenerationRequest;
import com.hngy.siae.ai.domain.dto.VideoGenerationRequest;
import com.hngy.siae.ai.domain.vo.ImageGenerationVO;
import com.hngy.siae.ai.domain.vo.VideoGenerationVO;
import com.hngy.siae.ai.service.MediaGenerationService;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import com.hngy.siae.security.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * 媒体生成控制器
 * 提供图片生成和视频生成的API接口
 */
@Slf4j
@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
@Tag(name = "媒体生成", description = "AI图片和视频生成接口")
public class MediaController {

    private final MediaGenerationService mediaGenerationService;
    private final SecurityUtil securityUtil;

    /**
     * 生成图片
     */
    @PostMapping("/image")
    @Operation(summary = "生成图片", description = "根据提示词生成图片，使用CogView-4模型")
    @SiaeAuthorize("isAuthenticated()")
    public Mono<Result<ImageGenerationVO>> generateImage(@Valid @RequestBody ImageGenerationRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        log.info("Image generation request from user: {}, model: {}", userId, request.getModel());

        return mediaGenerationService.generateImage(request, userId)
                .map(Result::success)
                .onErrorResume(e -> {
                    log.error("Image generation failed", e);
                    return Mono.just(Result.error("图片生成失败: " + e.getMessage()));
                });
    }

    /**
     * 生成视频（异步）
     */
    @PostMapping("/video")
    @Operation(summary = "生成视频", description = "根据提示词生成视频，使用CogVideoX-3模型，返回任务ID")
    @SiaeAuthorize("isAuthenticated()")
    public Mono<Result<VideoGenerationVO>> generateVideo(@Valid @RequestBody VideoGenerationRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        log.info("Video generation request from user: {}, model: {}", userId, request.getModel());

        return mediaGenerationService.generateVideo(request, userId)
                .map(Result::success)
                .onErrorResume(e -> {
                    log.error("Video generation failed", e);
                    return Mono.just(Result.error("视频生成失败: " + e.getMessage()));
                });
    }

    /**
     * 查询视频生成结果
     */
    @GetMapping("/video/{taskId}")
    @Operation(summary = "查询视频结果", description = "根据任务ID查询视频生成结果")
    @SiaeAuthorize("isAuthenticated()")
    public Mono<Result<VideoGenerationVO>> getVideoResult(
            @Parameter(description = "任务ID", required = true)
            @PathVariable String taskId) {
        Long userId = securityUtil.getCurrentUserId();
        log.info("Video result query from user: {}, taskId: {}", userId, taskId);

        return mediaGenerationService.getVideoResult(taskId, userId)
                .map(Result::success)
                .onErrorResume(e -> {
                    log.error("Failed to get video result", e);
                    return Mono.just(Result.error("查询视频结果失败: " + e.getMessage()));
                });
    }
}
