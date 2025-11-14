package com.hngy.siae.media.service.upload;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.MediaResultCodeEnum;
import com.hngy.siae.media.config.MediaProperties;
import com.hngy.siae.media.domain.dto.upload.*;
import com.hngy.siae.media.domain.entity.FileEntity;
import com.hngy.siae.media.domain.entity.MultipartPart;
import com.hngy.siae.media.domain.entity.Quota;
import com.hngy.siae.media.domain.entity.Upload;
import com.hngy.siae.media.domain.enums.FileStatus;
import com.hngy.siae.media.domain.enums.UploadStatus;
import com.hngy.siae.media.infrastructure.messaging.EventPublisher;
import com.hngy.siae.media.infrastructure.storage.StorageService;
import com.hngy.siae.media.repository.FileRepository;
import com.hngy.siae.media.repository.MultipartPartRepository;
import com.hngy.siae.media.repository.QuotaRepository;
import com.hngy.siae.media.repository.UploadRepository;
import com.hngy.siae.media.security.TenantContext;
import com.hngy.siae.media.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.IntStream;

/**
 * 上传服务
 *
 * @author SIAE Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService {

    private final FileRepository fileRepository;
    private final UploadRepository uploadRepository;
    private final QuotaRepository quotaRepository;
    private final MultipartPartRepository multipartPartRepository;
    private final StorageService storageService;
    private final MediaProperties mediaProperties;
    private final AuditService auditService;
    private final EventPublisher eventPublisher;
    
    @Qualifier("fileProcessExecutor")
    private final Executor fileProcessExecutor;

    /**
     * 初始化上传
     */
    @Transactional(rollbackFor = Exception.class)
    public UploadInitResponse initUpload(UploadInitRequest request) {
        log.info("Initializing upload for tenant: {}, filename: {}, size: {}", 
                request.getTenantId(), request.getFilename(), request.getSize());

        // 1. 验证租户权限和配额
        validateQuota(request.getTenantId(), request.getSize());

        // 2. 验证文件大小和类型
        validateFile(request);

        // 3. 创建文件实体
        FileEntity fileEntity = createFileEntity(request);
        fileRepository.insert(fileEntity);

        // 4. 创建上传会话
        Upload upload = createUploadSession(request, fileEntity.getId());
        uploadRepository.insert(upload);

        // 5. 生成预签名 URL
        UploadInitResponse response = generatePresignedUrls(upload, fileEntity, request);

        log.info("Upload initialized successfully: uploadId={}, fileId={}", 
                upload.getUploadId(), fileEntity.getId());

        return response;
    }

    /**
     * 验证配额
     */
    private void validateQuota(String tenantId, Long fileSize) {
        Quota quota = quotaRepository.selectById(tenantId);
        if (quota == null) {
            // 创建默认配额
            quota = createDefaultQuota(tenantId);
            quotaRepository.insert(quota);
        }

        Map<String, Object> limits = quota.getLimits();
        Long maxBytes = getLongFromMap(limits, "max_bytes", getDefaultMaxBytes());
        Integer maxObjects = getIntFromMap(limits, "max_objects", getDefaultMaxObjects());

        AssertUtils.isTrue(quota.getBytesUsed() + fileSize <= maxBytes,
                MediaResultCodeEnum.STORAGE_QUOTA_EXCEEDED);
        AssertUtils.isTrue(quota.getObjectsCount() + 1 <= maxObjects,
                MediaResultCodeEnum.STORAGE_OBJECT_LIMIT_EXCEEDED);
    }

    /**
     * 验证文件
     */
    private void validateFile(UploadInitRequest request) {
        MediaProperties.Upload uploadProps = mediaProperties.getUpload();
        boolean multipartEnabled = request.getMultipart() != null && Boolean.TRUE.equals(request.getMultipart().getEnabled());
        Long configuredMax = multipartEnabled ? uploadProps.getMaxMultipartSize() : uploadProps.getMaxFileSize();
        long maxFileSize = configuredMax != null ? configuredMax : Long.MAX_VALUE;

        AssertUtils.isTrue(request.getSize() <= maxFileSize, MediaResultCodeEnum.FILE_SIZE_EXCEEDS_LIMIT);

        List<String> allowedMimeTypes = uploadProps.getAllowedMimeTypes();
        if (request.getMime() != null
                && allowedMimeTypes != null
                && !allowedMimeTypes.isEmpty()
                && !allowedMimeTypes.contains(request.getMime())) {
            AssertUtils.fail(MediaResultCodeEnum.FILE_TYPE_NOT_ALLOWED);
        }
    }

    /**
     * 创建文件实体
     */
    private FileEntity createFileEntity(UploadInitRequest request) {
        FileEntity fileEntity = new FileEntity();
        fileEntity.setTenantId(request.getTenantId());
        fileEntity.setOwnerId(TenantContext.getRequiredUserId());
        fileEntity.setSize(request.getSize());
        fileEntity.setMime(request.getMime());
        fileEntity.setStatus(FileStatus.INIT);
        fileEntity.setAcl(request.getAcl());
        fileEntity.setBizTags(request.getBizTags());
        fileEntity.setExt(request.getExt());
        fileEntity.setChecksum(convertChecksum(request.getChecksum()));
        
        // 生成存储路径
        String bucket = storageService.getTenantBucket(request.getTenantId());
        String storageKey = generateStorageKey(request.getTenantId(), request.getFilename());
        fileEntity.setBucket(bucket);
        fileEntity.setStorageKey(storageKey);
        
        return fileEntity;
    }

    /**
     * 创建上传会话
     */
    private Upload createUploadSession(UploadInitRequest request, String fileId) {
        Upload upload = new Upload();
        upload.setFileId(fileId);
        upload.setTenantId(request.getTenantId());
        
        boolean isMultipart = request.getMultipart() != null && request.getMultipart().getEnabled();
        upload.setMultipart(isMultipart);
        
        if (isMultipart) {
            Integer partSize = request.getMultipart().getPartSize();
            int totalParts = (int) Math.ceil((double) request.getSize() / partSize);
            upload.setPartSize(partSize);
            upload.setTotalParts(totalParts);
            upload.setCompletedParts(0);
        }
        
        upload.setStatus(UploadStatus.INIT);
        upload.setExpireAt(LocalDateTime.now().plusSeconds(
                mediaProperties.getUpload().getPresignedUrlExpiry()));
        
        return upload;
    }

    /**
     * 生成预签名 URL（优化：批量并行生成）
     */
    private UploadInitResponse generatePresignedUrls(Upload upload, FileEntity fileEntity, UploadInitRequest request) {
        UploadInitResponse response = new UploadInitResponse();
        response.setUploadId(upload.getUploadId());
        response.setFileId(fileEntity.getId());
        response.setBucket(fileEntity.getBucket());
        response.setExpireAt(upload.getExpireAt());

        // 确保存储桶存在
        storageService.ensureBucketExists(fileEntity.getBucket());

        List<UploadInitResponse.PartInfo> parts = new ArrayList<>();
        
        if (upload.getMultipart()) {
            // 分片上传：批量并行生成预签名 URL（性能优化）
            log.info("Generating {} presigned URLs for multipart upload", upload.getTotalParts());
            
            List<String> urls = storageService.batchGeneratePartUploadUrls(
                    fileEntity.getBucket(),
                    fileEntity.getStorageKey(),
                    upload.getTotalParts(),
                    mediaProperties.getUpload().getPresignedUrlExpiry()
            );
            
            for (int i = 0; i < urls.size(); i++) {
                UploadInitResponse.PartInfo partInfo = new UploadInitResponse.PartInfo();
                partInfo.setPartNumber(i + 1);
                partInfo.setUrl(urls.get(i));
                partInfo.setExpiresAt(upload.getExpireAt());
                parts.add(partInfo);
            }
        } else {
            // 单文件上传
            String url = storageService.generatePresignedUploadUrl(
                    fileEntity.getBucket(),
                    fileEntity.getStorageKey(),
                    mediaProperties.getUpload().getPresignedUrlExpiry()
            );
            
            UploadInitResponse.PartInfo partInfo = new UploadInitResponse.PartInfo();
            partInfo.setPartNumber(1);
            partInfo.setUrl(url);
            partInfo.setExpiresAt(upload.getExpireAt());
            parts.add(partInfo);
        }
        
        response.setParts(parts);
        response.setHeaders(new HashMap<>());
        
        return response;
    }

    /**
     * 生成存储键
     */
    private String generateStorageKey(String tenantId, String filename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String sanitizedFilename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        return String.format("%s/%s/%s", tenantId, timestamp, sanitizedFilename);
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
        limits.put("max_bytes", mediaProperties.getQuota().getDefaultMaxBytes());
        limits.put("max_objects", mediaProperties.getQuota().getDefaultMaxObjects());
        limits.put("daily_download", "unlimited");
        quota.setLimits(limits);
        
        quota.setResetStrategy("monthly");
        return quota;
    }

    /**
     * 转换校验和格式
     */
    private Map<String, Object> convertChecksum(Map<String, String> checksum) {
        if (checksum == null) {
            return null;
        }
        return new HashMap<>(checksum);
    }

    /**
     * 从 Map 中获取 Long 值
     */
    private Long getLongFromMap(Map<String, Object> map, String key, Long defaultValue) {
        if (map == null) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.valueOf(value.toString());
    }

    /**
     * 从 Map 中获取 Integer 值
     */
    private Integer getIntFromMap(Map<String, Object> map, String key, Integer defaultValue) {
        if (map == null) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.valueOf(value.toString());
    }

    private Long getDefaultMaxBytes() {
        Long configured = mediaProperties.getQuota().getDefaultMaxBytes();
        return configured != null ? configured : Long.MAX_VALUE;
    }

    private Integer getDefaultMaxObjects() {
        Integer configured = mediaProperties.getQuota().getDefaultMaxObjects();
        return configured != null ? configured : Integer.MAX_VALUE;
    }

    /**
     * 刷新上传 URL
     */
    public UploadRefreshResponse refreshUpload(
            String uploadId, 
            UploadRefreshRequest request) {
        log.info("Refreshing upload URLs for uploadId: {}", uploadId);

        // 1. 查询上传会话
        Upload upload = uploadRepository.selectById(uploadId);
        AssertUtils.notNull(upload, MediaResultCodeEnum.UPLOAD_SESSION_NOT_FOUND);
        AssertUtils.isTrue(upload.getStatus() == UploadStatus.INIT || upload.getStatus() == UploadStatus.IN_PROGRESS,
                MediaResultCodeEnum.UPLOAD_STATUS_INVALID);

        // 2. 查询文件实体
        FileEntity fileEntity = fileRepository.selectById(upload.getFileId());
        AssertUtils.notNull(fileEntity, MediaResultCodeEnum.FILE_NOT_FOUND);

        // 3. 生成新的预签名 URL
        UploadRefreshResponse response =
                new UploadRefreshResponse();
        response.setUploadId(uploadId);
        
        LocalDateTime newExpireAt = LocalDateTime.now().plusSeconds(
                mediaProperties.getUpload().getPresignedUrlExpiry());
        response.setExpiresAt(newExpireAt);

        List<UploadRefreshResponse.PartInfo> parts = new ArrayList<>();
        
        for (UploadRefreshRequest.PartRequest partRequest : request.getParts()) {
            String url = storageService.generatePartUploadUrl(
                    fileEntity.getBucket(),
                    fileEntity.getStorageKey(),
                    partRequest.getPartNumber(),
                    mediaProperties.getUpload().getPresignedUrlExpiry()
            );
            
            UploadRefreshResponse.PartInfo partInfo =
                    new UploadRefreshResponse.PartInfo();
            partInfo.setPartNumber(partRequest.getPartNumber());
            partInfo.setUrl(url);
            partInfo.setExpiresAt(newExpireAt);
            parts.add(partInfo);
        }
        
        response.setParts(parts);

        // 4. 更新上传会话过期时间
        upload.setExpireAt(newExpireAt);
        uploadRepository.updateById(upload);

        log.info("Upload URLs refreshed successfully for uploadId: {}", uploadId);
        return response;
    }

    /**
     * 完成上传（优化：异步合并分片）
     */
    @Transactional(rollbackFor = Exception.class)
    public UploadCompleteResponse completeUpload(String uploadId, UploadCompleteRequest request) {
        log.info("Completing upload for uploadId: {}", uploadId);

        // 1. 查询上传会话
        Upload upload = uploadRepository.selectById(uploadId);
        AssertUtils.notNull(upload, MediaResultCodeEnum.UPLOAD_SESSION_NOT_FOUND);
        AssertUtils.isTrue(upload.getStatus() != UploadStatus.COMPLETED,
                MediaResultCodeEnum.UPLOAD_ALREADY_COMPLETED);

        // 2. 查询文件实体
        FileEntity fileEntity = fileRepository.selectById(upload.getFileId());
        AssertUtils.notNull(fileEntity, MediaResultCodeEnum.FILE_NOT_FOUND);

        // 3. 验证分片信息（如果是分片上传）
        if (upload.getMultipart()) {
            validateAndSaveParts(upload, request);
        }

        // 4. 验证文件完整性
        validateFileIntegrity(fileEntity, request);

        // 5. 更新状态为"处理中"
        if (upload.getMultipart()) {
            fileEntity.setStatus(FileStatus.PROCESSING);
            upload.setStatus(UploadStatus.PROCESSING);
        } else {
            // 单文件上传直接完成
            fileEntity.setStatus(FileStatus.COMPLETED);
            upload.setStatus(UploadStatus.COMPLETED);
        }
        
        fileRepository.updateById(fileEntity);
        uploadRepository.updateById(upload);

        // 6. 异步合并分片（不阻塞请求）
        if (upload.getMultipart()) {
            log.info("Starting async merge for uploadId: {}", uploadId);
            asyncMergeMultipartObject(fileEntity, upload);
        } else {
            // 单文件上传立即更新配额和发布事件
            updateQuotaUsage(fileEntity.getTenantId(), fileEntity.getSize(), 1);
            
            // 记录审计日志
            Map<String, Object> auditMetadata = new HashMap<>();
            auditMetadata.put("uploadId", uploadId);
            auditMetadata.put("size", fileEntity.getSize());
            auditService.logUploadComplete(fileEntity.getId(), fileEntity.getTenantId(), 
                    fileEntity.getOwnerId(), auditMetadata);
            
            publishFileUploadedEvent(fileEntity);
        }

        log.info("Upload request completed: uploadId={}, fileId={}, status={}", 
                uploadId, fileEntity.getId(), fileEntity.getStatus());

        UploadCompleteResponse response = new UploadCompleteResponse();
        response.setFileId(fileEntity.getId());
        response.setStatus(fileEntity.getStatus());
        return response;
    }

    /**
     * 验证并保存分片信息
     */
    private void validateAndSaveParts(Upload upload, UploadCompleteRequest request) {
        AssertUtils.notEmpty(request.getParts(), MediaResultCodeEnum.UPLOAD_PARTS_EMPTY);
        AssertUtils.isTrue(request.getParts().size() == upload.getTotalParts(),
                MediaResultCodeEnum.UPLOAD_PART_COUNT_MISMATCH);

        List<UploadCompleteRequest.PartInfo> sortedParts = new ArrayList<>(request.getParts());
        sortedParts.sort(Comparator.comparing(UploadCompleteRequest.PartInfo::getPartNumber));

        for (UploadCompleteRequest.PartInfo partInfo : sortedParts) {
            MultipartPart part = new MultipartPart();
            part.setUploadId(upload.getUploadId());
            part.setPartNumber(partInfo.getPartNumber());
            part.setEtag(partInfo.getEtag());
            part.setUploadedAt(LocalDateTime.now());
            multipartPartRepository.insert(part);
        }
    }

    /**
     * 验证文件完整性
     */
    private void validateFileIntegrity(FileEntity fileEntity, UploadCompleteRequest request) {
        if (request.getChecksum() == null) {
            return;
        }

        // 验证文件大小
        if (request.getChecksum().containsKey("size")) {
            String sizeStr = request.getChecksum().get("size");
            Long uploadedSize = Long.parseLong(sizeStr);
            AssertUtils.isTrue(uploadedSize.equals(fileEntity.getSize()),
                    MediaResultCodeEnum.UPLOAD_FILE_SIZE_MISMATCH);
        }

        // 验证 SHA256（如果提供）
        if (request.getChecksum().containsKey("sha256")) {
            String sha256 = request.getChecksum().get("sha256");
            fileEntity.setSha256(sha256);
        }
    }

    /**
     * 更新配额使用情况
     */
    private void updateQuotaUsage(String tenantId, Long bytes, int objectCount) {
        Quota quota = quotaRepository.selectById(tenantId);
        if (quota != null) {
            quota.setBytesUsed(quota.getBytesUsed() + bytes);
            quota.setObjectsCount(quota.getObjectsCount() + objectCount);
            quotaRepository.updateById(quota);
        }
    }

    /**
     * 中断上传
     */
    @Transactional(rollbackFor = Exception.class)
    public void abortUpload(String uploadId) {
        log.info("Aborting upload for uploadId: {}", uploadId);

        // 1. 查询上传会话
        Upload upload = uploadRepository.selectById(uploadId);
        AssertUtils.notNull(upload, MediaResultCodeEnum.UPLOAD_SESSION_NOT_FOUND);
        AssertUtils.isTrue(upload.getStatus() != UploadStatus.COMPLETED,
                MediaResultCodeEnum.UPLOAD_ALREADY_COMPLETED);

        // 2. 查询文件实体
        FileEntity fileEntity = fileRepository.selectById(upload.getFileId());
        AssertUtils.notNull(fileEntity, MediaResultCodeEnum.FILE_NOT_FOUND);

        // 3. 清理对象存储中的临时文件
        try {
            if (storageService.objectExists(fileEntity.getBucket(), fileEntity.getStorageKey())) {
                storageService.deleteObject(fileEntity.getBucket(), fileEntity.getStorageKey());
                log.info("Deleted temporary object: {}/{}", fileEntity.getBucket(), fileEntity.getStorageKey());
            }
        } catch (Exception e) {
            log.error("Failed to delete temporary object", e);
        }

        // 4. 删除分片记录
        if (upload.getMultipart()) {
            LambdaQueryWrapper<MultipartPart> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MultipartPart::getUploadId, uploadId);
            multipartPartRepository.delete(wrapper);

            deletePartObjects(fileEntity, upload);
        }

        // 5. 更新上传会话状态
        upload.setStatus(UploadStatus.ABORTED);
        uploadRepository.updateById(upload);

        // 6. 更新文件状态
        fileEntity.setStatus(FileStatus.FAILED);
        fileRepository.updateById(fileEntity);

        log.info("Upload aborted successfully: uploadId={}, fileId={}", uploadId, fileEntity.getId());
    }

    /**
     * 异步合并分片（性能优化：不阻塞请求）
     */
    private void asyncMergeMultipartObject(FileEntity fileEntity, Upload upload) {
        CompletableFuture.runAsync(() -> {
            try {
                log.info("Starting merge for fileId: {}, uploadId: {}", 
                        fileEntity.getId(), upload.getUploadId());
                
                // 合并分片
                List<String> partKeys = buildPartObjectKeys(fileEntity, upload);
                storageService.composeObject(fileEntity.getBucket(), partKeys, fileEntity.getStorageKey());
                
                // 删除临时分片
                storageService.deleteObjects(fileEntity.getBucket(), partKeys);
                
                log.info("Merge completed for fileId: {}", fileEntity.getId());
                
                // 更新状态为"完成"
                fileEntity.setStatus(FileStatus.COMPLETED);
                fileRepository.updateById(fileEntity);
                
                upload.setStatus(UploadStatus.COMPLETED);
                uploadRepository.updateById(upload);
                
                // 更新租户配额
                updateQuotaUsage(fileEntity.getTenantId(), fileEntity.getSize(), 1);
                
                // 记录审计日志
                Map<String, Object> auditMetadata = new HashMap<>();
                auditMetadata.put("uploadId", upload.getUploadId());
                auditMetadata.put("size", fileEntity.getSize());
                auditService.logUploadComplete(fileEntity.getId(), fileEntity.getTenantId(), 
                        fileEntity.getOwnerId(), auditMetadata);
                
                // 发布文件上传完成事件
                publishFileUploadedEvent(fileEntity);
                
                log.info("File processing completed: fileId={}", fileEntity.getId());
                
            } catch (Exception e) {
                log.error("Failed to merge multipart object for fileId: {}", fileEntity.getId(), e);
                
                // 更新状态为"失败"
                fileEntity.setStatus(FileStatus.FAILED);
                fileRepository.updateById(fileEntity);
                
                upload.setStatus(UploadStatus.FAILED);
                uploadRepository.updateById(upload);
            }
        }, fileProcessExecutor);
    }

    /**
     * 同步合并分片（保留用于单元测试或特殊场景）
     */
    private void mergeMultipartObject(FileEntity fileEntity, Upload upload) {
        List<String> partKeys = buildPartObjectKeys(fileEntity, upload);
        storageService.composeObject(fileEntity.getBucket(), partKeys, fileEntity.getStorageKey());
        storageService.deleteObjects(fileEntity.getBucket(), partKeys);
    }

    private void deletePartObjects(FileEntity fileEntity, Upload upload) {
        List<String> partKeys = buildPartObjectKeys(fileEntity, upload);
        for (String partKey : partKeys) {
            try {
                storageService.deleteObject(fileEntity.getBucket(), partKey);
            } catch (RuntimeException e) {
                log.warn("Failed to delete part object {}, ignoring. Reason: {}", partKey, e.getMessage());
            }
        }
    }

    private List<String> buildPartObjectKeys(FileEntity fileEntity, Upload upload) {
        return IntStream.rangeClosed(1, upload.getTotalParts())
                .mapToObj(i -> storageService.buildPartObjectKey(fileEntity.getStorageKey(), i))
                .toList();
    }

    /**
     * 发布文件上传完成事件
     */
    private void publishFileUploadedEvent(FileEntity fileEntity) {
        try {
            com.hngy.siae.media.domain.event.FileEvent.FileInfo fileInfo = 
                    com.hngy.siae.media.domain.event.FileEvent.FileInfo.builder()
                    .bucket(fileEntity.getBucket())
                    .storageKey(fileEntity.getStorageKey())
                    .size(fileEntity.getSize())
                    .mime(fileEntity.getMime())
                    .sha256(fileEntity.getSha256())
                    .ownerId(fileEntity.getOwnerId())
                    .build();

            com.hngy.siae.media.domain.event.FileEvent event = 
                    com.hngy.siae.media.domain.event.FileEvent.builder()
                    .eventId(java.util.UUID.randomUUID().toString())
                    .eventType("file.uploaded")
                    .fileId(fileEntity.getId())
                    .tenantId(fileEntity.getTenantId())
                    .fileInfo(fileInfo)
                    .timestamp(LocalDateTime.now())
                    .metadata(new HashMap<>())
                    .build();

            eventPublisher.publishFileEvent(event);
            log.info("Published file.uploaded event: fileId={}", fileEntity.getId());
        } catch (Exception e) {
            log.error("Failed to publish file.uploaded event: fileId={}", fileEntity.getId(), e);
        }
    }

}
