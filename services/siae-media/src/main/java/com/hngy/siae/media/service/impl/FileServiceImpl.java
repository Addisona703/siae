package com.hngy.siae.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.exception.BusinessException;
import com.hngy.siae.core.result.MediaResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.media.config.MediaProperties;
import com.hngy.siae.media.domain.dto.file.*;
import com.hngy.siae.media.domain.entity.FileEntity;
import com.hngy.siae.media.domain.enums.FileStatus;
import com.hngy.siae.media.mapper.FileMapper;
import com.hngy.siae.media.service.IAuditService;
import com.hngy.siae.media.service.IFileService;
import com.hngy.siae.media.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 文件服务实现
 *
 * @author SIAE Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements IFileService {

    private final FileMapper fileMapper;
    private final IAuditService auditService;
    private final StorageService storageService;
    private final MediaProperties mediaProperties;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 查询文件列表
     */
    @Override
    public PageVO<FileInfoVO> queryFiles(PageDTO<FileQueryDTO> pageDTO) {
        FileQueryDTO query = pageDTO.getParams();
        
        log.info("Querying files with params: {}", query);
        
        // 构建查询条件
        LambdaQueryWrapper<FileEntity> wrapper = new LambdaQueryWrapper<>();
        
        if (query != null) {
            wrapper.eq(query.getTenantId() != null, FileEntity::getTenantId, query.getTenantId())
                   .eq(query.getOwnerId() != null, FileEntity::getOwnerId, query.getOwnerId())
                   .eq(query.getStatus() != null, FileEntity::getStatus, query.getStatus())
                   .ge(query.getCreatedFrom() != null, FileEntity::getCreatedAt, query.getCreatedFrom())
                   .le(query.getCreatedTo() != null, FileEntity::getCreatedAt, query.getCreatedTo())
                   .isNull(FileEntity::getDeletedAt); // 只查询未删除的文件
            
            // 处理标签筛选
            if (query.getBizTags() != null && !query.getBizTags().isEmpty()) {
                // 使用 JSON 查询（需要数据库支持）
                for (String tag : query.getBizTags()) {
                    wrapper.apply("JSON_CONTAINS(biz_tags, JSON_QUOTE({0}))", tag);
                }
            }
            
            // 排序
            if ("created_at".equals(query.getOrderBy())) {
                if ("asc".equalsIgnoreCase(query.getOrder())) {
                    wrapper.orderByAsc(FileEntity::getCreatedAt);
                } else {
                    wrapper.orderByDesc(FileEntity::getCreatedAt);
                }
            } else if ("size".equals(query.getOrderBy())) {
                if ("asc".equalsIgnoreCase(query.getOrder())) {
                    wrapper.orderByAsc(FileEntity::getSize);
                } else {
                    wrapper.orderByDesc(FileEntity::getSize);
                }
            }
        } else {
            wrapper.isNull(FileEntity::getDeletedAt)
                   .orderByDesc(FileEntity::getCreatedAt);
        }
        
        // 分页查询
        Page<FileEntity> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        Page<FileEntity> result = fileMapper.selectPage(page, wrapper);
        
        log.info("Found {} files, total: {}", result.getRecords().size(), result.getTotal());
        
        // 使用 BeanConvertUtil 批量转换
        List<FileInfoVO> records = BeanConvertUtil.toList(result.getRecords(), FileInfoVO.class);
        
        // 设置 fileId（因为 Entity 的 id 字段需要映射到 Response 的 fileId）
        for (int i = 0; i < records.size(); i++) {
            records.get(i).setFileId(result.getRecords().get(i).getId());
        }
        
        PageVO<FileInfoVO> pageVO = new PageVO<>();
        pageVO.setRecords(records);
        pageVO.setTotal(result.getTotal());
        pageVO.setPageNum((int) result.getCurrent());
        pageVO.setPageSize((int) result.getSize());
        
        return pageVO;
    }

    /**
     * 获取文件详情
     */
    @Override
    public FileInfoVO getFileById(String fileId) {
        FileEntity fileEntity = fileMapper.selectById(fileId);
        AssertUtils.notNull(fileEntity, MediaResultCodeEnum.FILE_NOT_FOUND);
        AssertUtils.isNull(fileEntity.getDeletedAt(), MediaResultCodeEnum.FILE_ALREADY_DELETED);
        
        // 使用 BeanConvertUtil 转换
        FileInfoVO response = BeanConvertUtil.to(fileEntity, FileInfoVO.class);
        response.setFileId(fileEntity.getId());
        return response;
    }

    /**
     * 更新文件元数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileInfoVO updateFile(String fileId, FileUpdateDTO request) {
        log.info("Updating file metadata: fileId={}", fileId);
        
        // 查询文件
        FileEntity fileEntity = fileMapper.selectById(fileId);
        AssertUtils.notNull(fileEntity, MediaResultCodeEnum.FILE_NOT_FOUND);
        AssertUtils.isNull(fileEntity.getDeletedAt(), MediaResultCodeEnum.FILE_ALREADY_DELETED);
        
        // 记录变更前的状态
        Map<String, Object> changes = new HashMap<>();
        
        // 更新访问策略
        if (request.getAccessPolicy() != null && request.getAccessPolicy() != fileEntity.getAccessPolicy()) {
            Map<String, Object> policyChange = new HashMap<>();
            policyChange.put("old", fileEntity.getAccessPolicy());
            policyChange.put("new", request.getAccessPolicy());
            changes.put("accessPolicy", policyChange);
            fileEntity.setAccessPolicy(request.getAccessPolicy());
            
            // 清除URL缓存（因为访问策略变更会影响URL生成）
            String cacheKey = buildUrlCacheKey(fileId);
            redisTemplate.delete(cacheKey);
            log.info("Cleared URL cache for fileId: {} due to access policy change", fileId);
        }
        
        // 更新元数据
        if (request.getBizTags() != null) {
            Map<String, Object> bizTagsChange = new HashMap<>();
            bizTagsChange.put("old", fileEntity.getBizTags());
            bizTagsChange.put("new", request.getBizTags());
            changes.put("bizTags", bizTagsChange);
            fileEntity.setBizTags(request.getBizTags());
        }
        
        // Note: ACL field is not implemented in FileEntity yet
        // TODO: Add ACL support in future version
        if (request.getAcl() != null) {
            log.warn("ACL update requested but not supported yet: fileId={}", fileId);
        }
        
        if (request.getExt() != null) {
            Map<String, Object> extChange = new HashMap<>();
            extChange.put("old", fileEntity.getExt());
            extChange.put("new", request.getExt());
            changes.put("ext", extChange);
            fileEntity.setExt(request.getExt());
        }
        
        if (!changes.isEmpty()) {
            fileMapper.updateById(fileEntity);
            
            // 记录审计日志
            Map<String, Object> auditMetadata = new HashMap<>();
            auditMetadata.put("changes", changes);
            auditService.logFileUpdate(fileId, fileEntity.getTenantId(), 
                    fileEntity.getOwnerId(), auditMetadata);
            
            log.info("File metadata updated successfully: fileId={}", fileId);
        }
        
        // 使用 BeanConvertUtil 转换
        FileInfoVO response = BeanConvertUtil.to(fileEntity, FileInfoVO.class);
        response.setFileId(fileEntity.getId());
        return response;
    }

    /**
     * 软删除文件
     * - 设置 deleted_at 字段
     * - 从对象存储删除实际文件
     * - 清除 URL 缓存
     * - 记录审计日志
     * 
     * 使用事务确保数据一致性：
     * - 如果对象存储删除失败，会回滚数据库操作
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(String fileId) {
        log.info("Deleting file: fileId={}", fileId);
        
        // 查询文件
        FileEntity fileEntity = fileMapper.selectById(fileId);
        AssertUtils.notNull(fileEntity, MediaResultCodeEnum.FILE_NOT_FOUND);
        AssertUtils.isNull(fileEntity.getDeletedAt(), MediaResultCodeEnum.FILE_ALREADY_DELETED);
        
        try {
            // 1. 从对象存储删除实际文件
            log.info("Deleting file from object storage: bucket={}, key={}", 
                    fileEntity.getBucket(), fileEntity.getStorageKey());
            storageService.deleteObject(fileEntity.getBucket(), fileEntity.getStorageKey());
            log.info("File deleted from object storage successfully");
            
            // 2. 软删除数据库记录
            fileEntity.setDeletedAt(java.time.LocalDateTime.now());
            fileEntity.setStatus(FileStatus.DELETED);
            fileMapper.updateById(fileEntity);
            log.info("File marked as deleted in database");
            
            // 3. 清除 URL 缓存
            String cacheKey = buildUrlCacheKey(fileId);
            redisTemplate.delete(cacheKey);
            log.info("Cleared URL cache for fileId: {}", fileId);
            
            // 4. 记录审计日志
            Map<String, Object> auditMetadata = new HashMap<>();
            auditMetadata.put("size", fileEntity.getSize());
            auditMetadata.put("mime", fileEntity.getMime());
            auditMetadata.put("bucket", fileEntity.getBucket());
            auditMetadata.put("storageKey", fileEntity.getStorageKey());
            auditService.logFileDelete(fileId, fileEntity.getTenantId(), 
                    fileEntity.getOwnerId(), auditMetadata);
            
            log.info("File deleted successfully: fileId={}", fileId);
        } catch (Exception e) {
            log.error("Failed to delete file: fileId={}", fileId, e);
            // 事务会自动回滚数据库操作
            throw new RuntimeException("Failed to delete file from storage: " + e.getMessage(), e);
        }
    }

    /**
     * 恢复文件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileInfoVO restoreFile(String fileId) {
        log.info("Restoring file: fileId={}", fileId);
        
        // 查询文件
        FileEntity fileEntity = fileMapper.selectById(fileId);
        AssertUtils.notNull(fileEntity, MediaResultCodeEnum.FILE_NOT_FOUND);
        AssertUtils.notNull(fileEntity.getDeletedAt(), MediaResultCodeEnum.FILE_NOT_DELETED);
        
        // 恢复文件
        fileEntity.setDeletedAt(null);
        fileEntity.setStatus(FileStatus.COMPLETED);
        fileMapper.updateById(fileEntity);
        
        // 记录审计日志
        Map<String, Object> auditMetadata = new HashMap<>();
        auditMetadata.put("size", fileEntity.getSize());
        auditMetadata.put("mime", fileEntity.getMime());
        auditService.logFileRestore(fileId, fileEntity.getTenantId(), 
                fileEntity.getOwnerId(), auditMetadata);
        
        log.info("File restored successfully: fileId={}", fileId);
        
        // 使用 BeanConvertUtil 转换
        FileInfoVO response = BeanConvertUtil.to(fileEntity, FileInfoVO.class);
        response.setFileId(fileEntity.getId());
        return response;
    }

    /**
     * 获取单个文件的访问 URL
     * 根据文件的访问策略返回不同类型的 URL：
     * - PUBLIC: 返回永久 URL（不带签名）
     * - PRIVATE: 返回临时签名 URL
     * 
     * @param fileId 文件ID
     * @param expirySeconds URL过期时间（秒），仅对私有文件有效
     * @return 文件访问 URL
     */
    @Override
    public String getFileUrl(String fileId, Integer expirySeconds) {
        log.info("Getting file URL: fileId={}, expirySeconds={}", fileId, expirySeconds);
        
        // 使用默认过期时间
        if (expirySeconds == null || expirySeconds <= 0) {
            expirySeconds = mediaProperties.getUrl().getExpiration();
        }
        
        // 先查询文件是否存在
        FileEntity file = fileMapper.selectById(fileId);
        if (file == null) {
            throw new BusinessException(404, "文件不存在");
        }
        
        // 检查文件状态
        if (file.getStatus() != FileStatus.COMPLETED) {
            throw new BusinessException(400, "文件未完成上传，无法获取URL");
        }
        
        if (file.getDeletedAt() != null) {
            throw new BusinessException(404, "文件已删除");
        }
        
        // 批量获取（复用现有逻辑）
        BatchUrlDTO request = new BatchUrlDTO();
        request.setFileIds(List.of(fileId));
        request.setExpirySeconds(expirySeconds);
        
        BatchUrlVO result = batchGetFileUrls(request);
        String url = result.getUrls().get(fileId);
        
        if (url == null) {
            log.error("Failed to generate URL for fileId: {}", fileId);
            throw new BusinessException(500, "生成文件URL失败");
        }
        
        return url;
    }

    /**
     * 批量获取文件访问URL
     * 根据文件的访问策略生成不同类型的URL：
     * - PUBLIC: 生成永久公开URL（不带签名）
     * - PRIVATE/PROTECTED: 生成临时签名URL
     * 
     * @param request 批量URL请求参数
     * @return 包含URL映射和统计信息的响应对象
     */
    @Override
    public BatchUrlVO batchGetFileUrls(BatchUrlDTO request) {
        List<String> fileIds = request.getFileIds();
        Integer expirySeconds = request.getExpirySeconds();
        
        // 验证输入参数
        if (fileIds == null || fileIds.isEmpty()) {
            log.warn("Batch get file URLs called with empty fileIds");
            return com.hngy.siae.media.domain.dto.file.BatchUrlVO.builder()
                    .urls(new HashMap<>())
                    .expiresAt(java.time.LocalDateTime.now().plusSeconds(expirySeconds != null ? expirySeconds : 86400))
                    .successCount(0)
                    .failedCount(0)
                    .build();
        }
        
        log.info("Batch getting file URLs for {} files", fileIds.size());
        
        Map<String, String> result = new HashMap<>();
        List<String> missedIds = new ArrayList<>();
        
        // 如果启用了缓存，先从Redis批量查询
        if (Boolean.TRUE.equals(mediaProperties.getUrl().getCacheEnabled())) {
            for (String fileId : fileIds) {
                String cacheKey = buildUrlCacheKey(fileId);
                String cachedUrl = redisTemplate.opsForValue().get(cacheKey);
                if (cachedUrl != null) {
                    result.put(fileId, cachedUrl);
                    log.debug("Cache hit for fileId: {}", fileId);
                } else {
                    missedIds.add(fileId);
                }
            }
            log.info("Cache hit: {}/{}, missed: {}", result.size(), fileIds.size(), missedIds.size());
        } else {
            missedIds.addAll(fileIds);
        }
        
        // 为未命中的文件生成URL
        if (!missedIds.isEmpty()) {
            // 过滤掉null和空字符串，避免MyBatis异常
            List<String> validIds = missedIds.stream()
                    .filter(id -> id != null && !id.trim().isEmpty())
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
            
            if (validIds.isEmpty()) {
                log.warn("All missed IDs are invalid (null or empty)");
                return com.hngy.siae.media.domain.dto.file.BatchUrlVO.builder()
                        .urls(result)
                        .expiresAt(java.time.LocalDateTime.now().plusSeconds(expirySeconds))
                        .successCount(result.size())
                        .failedCount(fileIds.size() - result.size())
                        .build();
            }
            
            List<FileEntity> files;
            try {
                files = fileMapper.selectBatchIds(validIds);
            } catch (org.mybatis.spring.MyBatisSystemException e) {
                // 捕获 MyBatis 异常，通常是 JSON 反序列化失败
                log.error("Failed to query files by IDs, possibly due to invalid JSON format in biz_tags field. " +
                        "Please check database data format. IDs: {}", validIds, e);
                // 返回已有的缓存结果
                return com.hngy.siae.media.domain.dto.file.BatchUrlVO.builder()
                        .urls(result)
                        .expiresAt(java.time.LocalDateTime.now().plusSeconds(expirySeconds))
                        .successCount(result.size())
                        .failedCount(fileIds.size() - result.size())
                        .build();
            }
            
            for (FileEntity file : files) {
                // 验证文件状态
                if (file.getStatus() != FileStatus.COMPLETED || file.getDeletedAt() != null) {
                    log.warn("File is not available for URL generation: fileId={}, status={}", 
                            file.getId(), file.getStatus());
                    continue;
                }
                
                try {
                    String url;
                    int cacheTtl;
                    
                    // 根据访问策略生成不同类型的URL
                    if (file.getAccessPolicy() != null && file.getAccessPolicy().isPublic()) {
                        // 公开文件：生成永久URL（不带签名）
                        url = storageService.generatePublicUrl(file.getBucket(), file.getStorageKey());
                        cacheTtl = 86400 * 7; // 公开URL缓存7天
                        log.debug("Generated public URL for fileId: {}", file.getId());
                    } else {
                        // 私有/受保护文件：生成临时签名URL
                        url = storageService.generatePresignedDownloadUrl(
                                file.getBucket(),
                                file.getStorageKey(),
                                expirySeconds
                        );
                        // 缓存时间比URL过期时间短，避免返回即将失效的URL
                        cacheTtl = Math.min(
                                mediaProperties.getUrl().getPrivateTtl(),
                                expirySeconds - 3600 // 至少提前1小时过期
                        );
                        log.debug("Generated presigned URL for fileId: {}, expiry: {}s", file.getId(), expirySeconds);
                    }
                    
                    result.put(file.getId(), url);
                    
                    // 缓存URL
                    if (Boolean.TRUE.equals(mediaProperties.getUrl().getCacheEnabled())) {
                        String cacheKey = buildUrlCacheKey(file.getId());
                        redisTemplate.opsForValue().set(cacheKey, url, cacheTtl, TimeUnit.SECONDS);
                        log.debug("Cached URL for fileId: {}, ttl: {}s", file.getId(), cacheTtl);
                    }
                } catch (Exception e) {
                    log.error("Failed to generate URL for fileId: {}", file.getId(), e);
                }
            }
        }
        
        log.info("Batch get file URLs completed: success={}, failed={}", 
                result.size(), fileIds.size() - result.size());
        
        // 构建响应对象
        return com.hngy.siae.media.domain.dto.file.BatchUrlVO.builder()
                .urls(result)
                .expiresAt(java.time.LocalDateTime.now().plusSeconds(expirySeconds))
                .successCount(result.size())
                .failedCount(fileIds.size() - result.size())
                .build();
    }

    /**
     * 预览文件
     * 直接以 inline 方式输出图片、PDF、文档等可预览文件
     * 
     * @param fileId 文件ID
     * @param response HTTP响应对象
     */
    @Override
    public void previewFile(String fileId, jakarta.servlet.http.HttpServletResponse response) {
        log.info("Previewing file: fileId={}", fileId);
        
        // 查询文件信息
        FileEntity fileEntity = fileMapper.selectById(fileId);
        AssertUtils.notNull(fileEntity, MediaResultCodeEnum.FILE_NOT_FOUND);
        AssertUtils.isNull(fileEntity.getDeletedAt(), MediaResultCodeEnum.FILE_ALREADY_DELETED);
        AssertUtils.isTrue(fileEntity.getStatus() == FileStatus.COMPLETED, 
                MediaResultCodeEnum.FILE_STATUS_INVALID);
        
        try {
            // 根据访问策略生成URL
            String fileUrl;
            if (fileEntity.getAccessPolicy() != null && fileEntity.getAccessPolicy().isPublic()) {
                // 公开文件：生成永久URL
                fileUrl = storageService.generatePublicUrl(fileEntity.getBucket(), fileEntity.getStorageKey());
            } else {
                // 私有文件：生成临时签名URL（1小时有效期）
                fileUrl = storageService.generatePresignedDownloadUrl(
                        fileEntity.getBucket(),
                        fileEntity.getStorageKey(),
                        3600 // 1小时
                );
            }
            
            // 设置响应头
            response.setContentType(fileEntity.getMime() != null ? fileEntity.getMime() : "application/octet-stream");
            response.setHeader("Content-Disposition", "inline; filename=\"" + 
                    java.net.URLEncoder.encode(fileEntity.getFilename(), "UTF-8") + "\"");
            
            // 对于公开文件，设置缓存控制
            if (fileEntity.getAccessPolicy() != null && fileEntity.getAccessPolicy().isPublic()) {
                response.setHeader("Cache-Control", "public, max-age=604800"); // 7天
            } else {
                response.setHeader("Cache-Control", "private, max-age=3600"); // 1小时
            }
            
            // 重定向到文件URL
            response.sendRedirect(fileUrl);
            
            log.info("File preview redirect sent: fileId={}, url={}", fileId, fileUrl);
            
        } catch (Exception e) {
            log.error("Failed to preview file: fileId={}", fileId, e);
            throw new RuntimeException("Failed to preview file: " + e.getMessage(), e);
        }
    }

    /**
     * 批量删除文件
     * 用于其他服务删除关联的媒体文件（如删除内容时清理关联的视频、图片等）
     * 
     * @param fileIds 文件ID列表
     * @return 批量删除结果，包含成功和失败的文件ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchDeleteVO batchDeleteFiles(List<String> fileIds) {
        log.info("Batch deleting files: count={}", fileIds.size());
        
        List<String> successIds = new ArrayList<>();
        List<String> failedIds = new ArrayList<>();
        
        for (String fileId : fileIds) {
            try {
                // 查询文件
                FileEntity fileEntity = fileMapper.selectById(fileId);
                if (fileEntity == null) {
                    log.warn("File not found, skipping: fileId={}", fileId);
                    failedIds.add(fileId);
                    continue;
                }
                
                if (fileEntity.getDeletedAt() != null) {
                    log.warn("File already deleted, skipping: fileId={}", fileId);
                    successIds.add(fileId); // 已删除的视为成功
                    continue;
                }
                
                // 1. 从对象存储删除实际文件
                try {
                    storageService.deleteObject(fileEntity.getBucket(), fileEntity.getStorageKey());
                    log.debug("File deleted from object storage: fileId={}", fileId);
                } catch (Exception e) {
                    log.warn("Failed to delete file from storage, continuing with db deletion: fileId={}, error={}", 
                            fileId, e.getMessage());
                    // 即使存储删除失败，也继续标记数据库记录为已删除
                }
                
                // 2. 软删除数据库记录
                fileEntity.setDeletedAt(java.time.LocalDateTime.now());
                fileEntity.setStatus(FileStatus.DELETED);
                fileMapper.updateById(fileEntity);
                
                // 3. 清除 URL 缓存
                String cacheKey = buildUrlCacheKey(fileId);
                redisTemplate.delete(cacheKey);
                
                // 4. 记录审计日志
                Map<String, Object> auditMetadata = new HashMap<>();
                auditMetadata.put("size", fileEntity.getSize());
                auditMetadata.put("mime", fileEntity.getMime());
                auditMetadata.put("batchDelete", true);
                auditService.logFileDelete(fileId, fileEntity.getTenantId(), 
                        fileEntity.getOwnerId(), auditMetadata);
                
                successIds.add(fileId);
                log.debug("File deleted successfully: fileId={}", fileId);
                
            } catch (Exception e) {
                log.error("Failed to delete file: fileId={}", fileId, e);
                failedIds.add(fileId);
            }
        }
        
        log.info("Batch delete completed: success={}, failed={}", successIds.size(), failedIds.size());
        
        return BatchDeleteVO.builder()
                .successIds(successIds)
                .failedIds(failedIds)
                .successCount(successIds.size())
                .failedCount(failedIds.size())
                .build();
    }

    /**
     * 获取文件字节数据
     * 供内部服务调用，用于获取文件内容（如AI服务分析图片）
     * 
     * @param fileId 文件ID
     * @return 文件字节数组
     */
    @Override
    public byte[] getFileBytes(String fileId) {
        log.info("Getting file bytes: fileId={}", fileId);
        
        // 查询文件信息
        FileEntity fileEntity = fileMapper.selectById(fileId);
        AssertUtils.notNull(fileEntity, MediaResultCodeEnum.FILE_NOT_FOUND);
        AssertUtils.isNull(fileEntity.getDeletedAt(), MediaResultCodeEnum.FILE_ALREADY_DELETED);
        AssertUtils.isTrue(fileEntity.getStatus() == FileStatus.COMPLETED, 
                MediaResultCodeEnum.FILE_STATUS_INVALID);
        
        try {
            // 从对象存储获取文件字节数据
            byte[] bytes = storageService.getObjectBytes(fileEntity.getBucket(), fileEntity.getStorageKey());
            log.info("Retrieved file bytes: fileId={}, size={} bytes", fileId, bytes.length);
            return bytes;
        } catch (Exception e) {
            log.error("Failed to get file bytes: fileId={}", fileId, e);
            throw new RuntimeException("Failed to get file bytes: " + e.getMessage(), e);
        }
    }

    /**
     * 构建URL缓存Key
     */
    private String buildUrlCacheKey(String fileId) {
        return "media:url:" + fileId;
    }

}
