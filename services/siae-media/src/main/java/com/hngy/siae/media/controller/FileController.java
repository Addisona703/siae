package com.hngy.siae.media.controller;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.media.domain.dto.file.FileInfoResponse;
import com.hngy.siae.media.domain.dto.file.FileQueryRequest;
import com.hngy.siae.media.domain.dto.file.FileUpdateRequest;
import com.hngy.siae.media.service.FileService;
import com.hngy.siae.media.service.PreviewService;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static com.hngy.siae.media.constant.MediaPermissions.*;

/**
 * 文件控制器
 *
 * @author SIAE Team
 */
@Slf4j
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "文件管理", description = "文件查询和管理相关接口")
public class FileController {

    private final FileService fileService;
    private final PreviewService previewService;

    /**
     * 查询文件列表
     */
    @PostMapping("/query")
    @Operation(summary = "查询文件列表", description = "分页查询文件，支持多条件筛选")
    @SiaeAuthorize("hasAuthority('" + MEDIA_FILE_QUERY + "')")
    public Result<PageVO<FileInfoResponse>> queryFiles(
            @Parameter(description = "分页查询参数")
            @Valid @RequestBody PageDTO<FileQueryRequest> pageDTO) {
        log.info("Received file query request: {}", pageDTO);
        PageVO<FileInfoResponse> result = fileService.queryFiles(pageDTO);
        return Result.success(result);
    }

    /**
     * 获取文件详情
     */
    @GetMapping("/{fileId}")
    @Operation(summary = "获取文件详情", description = "根据文件ID获取文件详细信息")
    @SiaeAuthorize("hasAuthority('" + MEDIA_FILE_QUERY + "')")
    public Result<FileInfoResponse> getFile(
            @Parameter(description = "文件ID")
            @PathVariable String fileId) {
        log.info("Received get file request: fileId={}", fileId);
        FileInfoResponse response = fileService.getFileById(fileId);
        return Result.success(response);
    }

    /**
     * 更新文件元数据
     */
    @PatchMapping("/{fileId}")
    @Operation(summary = "更新文件元数据", description = "更新文件的标签、ACL或扩展属性")
    @SiaeAuthorize("hasAuthority('" + MEDIA_FILE_MANAGE + "')")
    public Result<FileInfoResponse> updateFile(
            @Parameter(description = "文件ID")
            @PathVariable String fileId,
            @Valid @RequestBody FileUpdateRequest request) {
        log.info("Received update file request: fileId={}", fileId);
        FileInfoResponse response = fileService.updateFile(fileId, request);
        return Result.success(response);
    }

    /**
     * 删除文件（软删除）
     */
    @DeleteMapping("/{fileId}")
    @Operation(summary = "删除文件", description = "软删除文件，可以恢复")
    @SiaeAuthorize("hasAuthority('" + MEDIA_FILE_DELETE + "')")
    public Result<Void> deleteFile(
            @Parameter(description = "文件ID")
            @PathVariable String fileId) {
        log.info("Received delete file request: fileId={}", fileId);
        fileService.deleteFile(fileId);
        return Result.success();
    }

    /**
     * 恢复文件
     */
    @PostMapping("/{fileId}:restore")
    @Operation(summary = "恢复文件", description = "恢复已删除的文件")
    @SiaeAuthorize("hasAuthority('" + MEDIA_FILE_MANAGE + "')")
    public Result<FileInfoResponse> restoreFile(
            @Parameter(description = "文件ID")
            @PathVariable String fileId) {
        log.info("Received restore file request: fileId={}", fileId);
        FileInfoResponse response = fileService.restoreFile(fileId);
        return Result.success(response);
    }

    /**
     * 文件预览
     */
    @GetMapping("/{fileId}/preview")
    @Operation(summary = "预览文件", description = "直接以 inline 方式输出图片、PDF、文档等可预览文件")
    @SiaeAuthorize("hasAuthority('" + MEDIA_FILE_QUERY + "')")
    public void previewFile(
            @Parameter(description = "文件ID")
            @PathVariable String fileId,
            HttpServletResponse response) {
        log.info("Received preview file request: fileId={}", fileId);
        previewService.preview(fileId, response);
    }

}
