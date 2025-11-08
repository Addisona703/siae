package com.hngy.siae.media.service;

import cn.hutool.core.util.StrUtil;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.MediaResultCodeEnum;
import com.hngy.siae.media.domain.entity.FileEntity;
import com.hngy.siae.media.domain.enums.FileStatus;
import com.hngy.siae.media.infrastructure.storage.StorageService;
import com.hngy.siae.media.repository.FileRepository;
import com.hngy.siae.media.security.TenantContext;
import io.minio.GetObjectResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * 文件预览服务
 *
 * @author SIAE Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PreviewService {

    private static final Set<String> PREVIEWABLE_MIME = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "application/pdf",
            "text/plain",
            "text/markdown",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    );

    private final FileRepository fileRepository;
    private final StorageService storageService;

    /**
     * 将文件流式输出用于浏览器预览
     */
    public void preview(String fileId, HttpServletResponse response) {
        String tenantId = TenantContext.getRequiredTenantId();
        FileEntity file = fileRepository.selectById(fileId);
        AssertUtils.notNull(file, MediaResultCodeEnum.FILE_NOT_FOUND);
        AssertUtils.isNull(file.getDeletedAt(), MediaResultCodeEnum.FILE_ALREADY_DELETED);
        AssertUtils.isTrue(file.getStatus() == FileStatus.COMPLETED, MediaResultCodeEnum.FILE_STATUS_INVALID);
        AssertUtils.isTrue(tenantId.equals(file.getTenantId()), MediaResultCodeEnum.UNAUTHORIZED_FILE_ACCESS);

        String mime = StrUtil.blankToDefault(file.getMime(), MediaType.APPLICATION_OCTET_STREAM_VALUE);
        AssertUtils.isTrue(isPreviewable(mime), MediaResultCodeEnum.FILE_TYPE_NOT_ALLOWED);

        response.setContentType(mime);
        String filename = StrUtil.blankToDefault(resolveFilename(file), file.getId());
        String encodedName = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "inline; filename=\"" + encodedName + "\"");

        try (GetObjectResponse objectStream = storageService.getObject(file.getBucket(), file.getStorageKey())) {
            AssertUtils.notNull(objectStream, MediaResultCodeEnum.STORAGE_OPERATION_FAILED);
            StreamUtils.copy(objectStream, response.getOutputStream());
            response.flushBuffer();
            log.info("Preview success: fileId={}, mime={}", fileId, mime);
        } catch (IOException e) {
            log.error("Preview failed: fileId={}", fileId, e);
            AssertUtils.fail(MediaResultCodeEnum.STORAGE_OPERATION_FAILED);
        }
    }

    private boolean isPreviewable(String mime) {
        if (mime == null) {
            return false;
        }
        return PREVIEWABLE_MIME.stream().anyMatch(mime::equalsIgnoreCase) || mime.startsWith("image/");
    }

    private String resolveFilename(FileEntity file) {
        if (file.getExt() != null) {
            Object originalName = file.getExt().get("filename");
            if (originalName instanceof String && StrUtil.isNotBlank((String) originalName)) {
                return (String) originalName;
            }
        }
        String storageKey = file.getStorageKey();
        if (StrUtil.isNotBlank(storageKey)) {
            int idx = storageKey.lastIndexOf('/');
            return idx >= 0 ? storageKey.substring(idx + 1) : storageKey;
        }
        return null;
    }
}
