package com.hngy.siae.media.controller;

import com.hngy.siae.core.result.Result;
import com.hngy.siae.media.domain.dto.file.BatchDeleteVO;
import com.hngy.siae.media.domain.dto.file.BatchUrlDTO;
import com.hngy.siae.media.domain.dto.file.BatchUrlVO;
import com.hngy.siae.media.domain.dto.file.FileInfoVO;
import com.hngy.siae.media.service.IFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 媒体服务Feign接口控制器
 * 供其他服务内部调用，无需权限验证
 *
 * @author SIAE Team
 */
@Slf4j
@Tag(name = "媒体Feign接口", description = "媒体服务间调用API")
@RestController
@RequestMapping("/feign")
@Validated
@RequiredArgsConstructor
public class MediaFeignController {

    private final IFileService fileService;

    // ==================== 文件查询接口 ====================

    /**
     * 根据文件ID获取文件详情
     * 供其他服务调用，用于获取文件基本信息
     *
     * @param fileId 文件ID
     * @return 文件详情
     */
    @Operation(summary = "获取文件详情", description = "根据文件ID获取文件详细信息，供Feign调用")
    @GetMapping("/files/{fileId}")
    public Result<FileInfoVO> getFileById(
            @Parameter(description = "文件ID", required = true)
            @NotBlank @PathVariable String fileId) {
        log.info("Feign call: get file by id={}", fileId);
        FileInfoVO response = fileService.getFileById(fileId);
        return Result.success(response);
    }

    // ==================== 文件URL接口 ====================

    /**
     * 获取单个文件访问URL
     * 供其他服务调用，根据文件访问策略自动返回合适的URL
     * - PUBLIC文件：返回永久访问URL
     * - PRIVATE文件：返回预签名URL（带过期时间）
     *
     * @param fileId 文件ID
     * @param expirySeconds URL过期时间（秒），仅对PRIVATE文件生效，默认24小时
     * @return 文件访问URL
     */
    @Operation(summary = "获取文件URL", description = "根据文件访问策略自动返回合适的URL，供Feign调用")
    @GetMapping("/files/{fileId}/url")
    public Result<String> getFileUrl(
            @Parameter(description = "文件ID", required = true)
            @NotBlank @PathVariable String fileId,
            @Parameter(description = "URL过期时间（秒），仅对PRIVATE文件生效，默认24小时")
            @RequestParam(defaultValue = "86400") Integer expirySeconds) {
        log.info("Feign call: get file URL for id={}, expirySeconds={}", fileId, expirySeconds);
        String url = fileService.getFileUrl(fileId, expirySeconds);
        return Result.success(url);
    }

    /**
     * 批量获取文件访问URL
     * 供其他服务调用，根据每个文件的访问策略自动返回合适的URL
     * - PUBLIC文件：返回永久访问URL
     * - PRIVATE文件：返回预签名URL（带过期时间）
     *
     * @param request 批量URL请求参数
     * @return 文件ID到URL的映射
     */
    @Operation(summary = "批量获取文件URL", description = "根据文件访问策略批量返回合适的URL，供Feign调用")
    @PostMapping("/files/urls/batch")
    public Result<BatchUrlVO> batchGetFileUrls(
            @Parameter(description = "批量URL请求", required = true)
            @Valid @RequestBody BatchUrlDTO request) {
        log.info("Feign call: batch get file URLs, count={}", request.getFileIds().size());
        BatchUrlVO response = fileService.batchGetFileUrls(request);
        return Result.success(response);
    }

    // ==================== 文件删除接口 ====================

    /**
     * 删除单个文件
     * 供其他服务调用，用于删除关联的媒体文件
     *
     * @param fileId 文件ID
     * @return 删除结果
     */
    @Operation(summary = "删除文件", description = "删除单个文件，供Feign调用")
    @DeleteMapping("/files/{fileId}")
    public Result<Void> deleteFile(
            @Parameter(description = "文件ID", required = true)
            @NotBlank @PathVariable String fileId) {
        log.info("Feign call: delete file by id={}", fileId);
        fileService.deleteFile(fileId);
        return Result.success();
    }

    /**
     * 批量删除文件
     * 供其他服务调用，用于批量删除关联的媒体文件（如删除内容时清理关联的视频、图片等）
     * 注意：使用 POST 方法而非 DELETE，因为 DELETE 方法带 RequestBody 在某些环境下不被支持
     *
     * @param fileIds 文件ID列表
     * @return 删除结果，包含成功和失败的文件ID
     */
    @Operation(summary = "批量删除文件", description = "批量删除文件，供Feign调用")
    @PostMapping("/files/batch-delete")
    public Result<BatchDeleteVO> batchDeleteFiles(
            @Parameter(description = "文件ID列表", required = true)
            @RequestBody @Valid java.util.List<@NotBlank String> fileIds) {
        log.info("Feign call: batch delete files, count={}", fileIds.size());
        BatchDeleteVO response = fileService.batchDeleteFiles(fileIds);
        return Result.success(response);
    }
}
