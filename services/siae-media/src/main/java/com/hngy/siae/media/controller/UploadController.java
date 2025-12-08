package com.hngy.siae.media.controller;

import com.hngy.siae.core.result.Result;

import com.hngy.siae.media.domain.dto.upload.*;
import com.hngy.siae.media.service.UploadService;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static com.hngy.siae.media.constant.MediaPermissions.*;

/**
 * 上传控制器
 *
 * @author SIAE Team
 */
@Slf4j
@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
@Tag(name = "上传管理", description = "文件上传相关接口")
public class UploadController {

    private final UploadService uploadService;

    /**
     * 初始化上传
     */
    @PostMapping("/init")
    @Operation(summary = "初始化上传", description = "创建上传会话并获取预签名URL")
    @SiaeAuthorize("hasAuthority('" + MEDIA_UPLOAD + "')")
    public Result<UploadInitVO> initUpload(@Valid @RequestBody UploadInitDTO request) {
        log.info("Received upload init request: {}", request);
        UploadInitVO response = uploadService.initUpload(request);
        return Result.success(response);
    }

    /**
     * 刷新上传 URL
     */
    @PostMapping("/{uploadId}/refresh")
    @Operation(summary = "刷新上传URL", description = "刷新预签名URL或追加分片")
    @SiaeAuthorize("hasAuthority('" + MEDIA_UPLOAD + "')")
    public Result<UploadRefreshVO> refreshUpload(
            @PathVariable String uploadId,
            @Valid @RequestBody UploadRefreshDTO request) {
        log.info("Received upload refresh request for uploadId: {}", uploadId);
        UploadRefreshVO response =
                uploadService.refreshUpload(uploadId, request);
        return Result.success(response);
    }

    /**
     * 完成上传
     */
    @PostMapping("/{uploadId}/complete")
    @Operation(summary = "完成上传", description = "确认文件上传完成")
    @SiaeAuthorize("hasAuthority('" + MEDIA_UPLOAD + "')")
    public Result<UploadCompleteVO> completeUpload(
            @PathVariable String uploadId,
            @Valid @RequestBody UploadCompleteDTO request) {
        log.info("Received upload complete request for uploadId: {}", uploadId);
        UploadCompleteVO response =
                uploadService.completeUpload(uploadId, request);
        return Result.success(response);
    }

    /**
     * 中断上传
     */
    @PostMapping("/{uploadId}/abort")
    @Operation(summary = "中断上传", description = "中断上传并清理临时文件")
    @SiaeAuthorize("hasAuthority('" + MEDIA_UPLOAD + "')")
    public Result<Void> abortUpload(@PathVariable String uploadId) {
        log.info("Received upload abort request for uploadId: {}", uploadId);
        uploadService.abortUpload(uploadId);
        return Result.success();
    }

    /**
     * 查询上传状态
     * 用于异步合并分片后，前端轮询查询处理结果
     */
    @GetMapping("/{uploadId}/status")
    @Operation(summary = "查询上传状态", description = "查询上传处理状态，用于异步合并分片后轮询结果")
    @SiaeAuthorize("hasAuthority('" + MEDIA_UPLOAD + "')")
    public Result<UploadStatusVO> getUploadStatus(@PathVariable String uploadId) {
        log.debug("Received upload status query for uploadId: {}", uploadId);
        UploadStatusVO response = uploadService.getUploadStatus(uploadId);
        return Result.success(response);
    }
}
