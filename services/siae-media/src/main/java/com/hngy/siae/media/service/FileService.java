package com.hngy.siae.media.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.MediaResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.media.config.MediaProperties;
import com.hngy.siae.media.domain.dto.file.FileInfoResponse;
import com.hngy.siae.media.domain.dto.file.FileQueryRequest;
import com.hngy.siae.media.domain.dto.file.FileUpdateRequest;
import com.hngy.siae.media.domain.entity.FileEntity;
import com.hngy.siae.media.domain.enums.FileStatus;
import com.hngy.siae.media.infrastructure.storage.StorageService;
import com.hngy.siae.media.repository.FileRepository;
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
 * 文件服务
 *
 * @author SIAE Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final AuditService auditService;
    private final StorageService storageService;
    private final MediaProperties mediaProperties;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 查询文件列表
     */
    public PageVO<FileInfoResponse> queryFiles(PageDTO<FileQueryRequest> pageDTO) {
        FileQueryRequest query = pageDTO.getParams();
        
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
        Page<FileEntity> result = fileRepository.selectPage(page, wrapper);
        
        log.info("Found {} files, total: {}", result.getRecords().size(), result.getTotal());
        
        // 使用 BeanConvertUtil 批量转换
        List<FileInfoResponse> records = BeanConvertUtil.toList(result.getRecords(), FileInfoResponse.class);
        
        // 设置 fileId（因为 Entity 的 id 字段需要映射到 Response 的 fileId）
        for (int i = 0; i < records.size(); i++) {
            records.get(i).setFileId(result.getRecords().get(i).getId());
        }
        
        PageVO<FileInfoResponse> pageVO = new PageVO<>();
        pageVO.setRecords(records);
        pageVO.setTotal(result.getTotal());
        pageVO.setPageNum((int) result.getCurrent());
        pageVO.setPageSize((int) result.getSize());
        
        return pageVO;
    }

    /**
     * 获取文件详情
     */
    public FileInfoResponse getFileById(String fileId) {
        FileEntity fileEntity = fileRepository.selectById(fileId);
        AssertUtils.notNull(fileEntity, MediaResultCodeEnum.FILE_NOT_FOUND);
        AssertUtils.isNull(fileEntity.getDeletedAt(), MediaResultCodeEnum.FILE_ALREADY_DELETED);
        
        // 使用 BeanConvertUtil 转换
        FileInfoResponse response = BeanConvertUtil.to(fileEntity, FileInfoResponse.class);
        response.setFileId(fileEntity.getId());
        return response;
    }

    /**
     * 更新文件元数据
     */
    @Transactional(rollbackFor = Exception.class)
    public FileInfoResponse updateFile(String fileId, FileUpdateRequest request) {
        log.info("Updating file metadata: fileId={}", fileId);
        
        // 查询文件
        FileEntity fileEntity = fileRepository.selectById(fileId);
        AssertUtils.notNull(fileEntity, MediaResultCodeEnum.FILE_NOT_FOUND);
        AssertUtils.isNull(fileEntity.getDeletedAt(), MediaResultCodeEnum.FILE_ALREADY_DELETED);
        
        // 记录变更前的状态
        Map<String, Object> changes = new HashMap<>();
        
        // 更新元数据
        if (request.getBizTags() != null) {
            Map<String, Object> bizTagsChange = new HashMap<>();
            bizTagsChange.put("old", fileEntity.getBizTags());
            bizTagsChange.put("new", request.getBizTags());
            changes.put("bizTags", bizTagsChange);
            fileEntity.setBizTags(request.getBizTags());
        }
        
        if (request.getAcl() != null) {
            Map<String, Object> aclChange = new HashMap<>();
            aclChange.put("old", fileEntity.getAcl());
            aclChange.put("new", request.getAcl());
            changes.put("acl", aclChange);
            fileEntity.setAcl(request.getAcl());
        }
        
        if (request.getExt() != null) {
            Map<String, Object> extChange = new HashMap<>();
            extChange.put("old", fileEntity.getExt());
            extChange.put("new", request.getExt());
            changes.put("ext", extChange);
            fileEntity.setExt(request.getExt());
        }
        
        if (!changes.isEmpty()) {
            fileRepository.updateById(fileEntity);
            
            // 记录审计日志
            Map<String, Object> auditMetadata = new HashMap<>();
            auditMetadata.put("changes", changes);
            auditService.logFileUpdate(fileId, fileEntity.getTenantId(), 
                    fileEntity.getOwnerId(), auditMetadata);
            
            log.info("File metadata updated successfully: fileId={}", fileId);
        }
        
        // 使用 BeanConvertUtil 转换
        FileInfoResponse response = BeanConvertUtil.to(fileEntity, FileInfoResponse.class);
        response.setFileId(fileEntity.getId());
        return response;
    }

    /**
     * 软删除文件
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(String fileId) {
        log.info("Deleting file: fileId={}", fileId);
        
        // 查询文件
        FileEntity fileEntity = fileRepository.selectById(fileId);
        AssertUtils.notNull(fileEntity, MediaResultCodeEnum.FILE_NOT_FOUND);
        AssertUtils.isNull(fileEntity.getDeletedAt(), MediaResultCodeEnum.FILE_ALREADY_DELETED);
        
        // 软删除
        fileEntity.setDeletedAt(java.time.LocalDateTime.now());
        fileEntity.setStatus(FileStatus.DELETED);
        fileRepository.updateById(fileEntity);
        
        // 记录审计日志
        Map<String, Object> auditMetadata = new HashMap<>();
        auditMetadata.put("size", fileEntity.getSize());
        auditMetadata.put("mime", fileEntity.getMime());
        auditService.logFileDelete(fileId, fileEntity.getTenantId(), 
                fileEntity.getOwnerId(), auditMetadata);
        
        log.info("File deleted successfully: fileId={}", fileId);
    }

    /**
     * 恢复文件
     */
    @Transactional(rollbackFor = Exception.class)
    public FileInfoResponse restoreFile(String fileId) {
        log.info("Restoring file: fileId={}", fileId);
        
        // 查询文件
        FileEntity fileEntity = fileRepository.selectById(fileId);
        AssertUtils.notNull(fileEntity, MediaResultCodeEnum.FILE_NOT_FOUND);
        AssertUtils.notNull(fileEntity.getDeletedAt(), MediaResultCodeEnum.FILE_NOT_DELETED);
        
        // 恢复文件
        fileEntity.setDeletedAt(null);
        fileEntity.setStatus(FileStatus.COMPLETED);
        fileRepository.updateById(fileEntity);
        
        // 记录审计日志
        Map<String, Object> auditMetadata = new HashMap<>();
        auditMetadata.put("size", fileEntity.getSize());
        auditMetadata.put("mime", fileEntity.getMime());
        auditService.logFileRestore(fileId, fileEntity.getTenantId(), 
                fileEntity.getOwnerId(), auditMetadata);
        
        log.info("File restored successfully: fileId={}", fileId);
        
        // 使用 BeanConvertUtil 转换
        FileInfoResponse response = BeanConvertUtil.to(fileEntity, FileInfoResponse.class);
        response.setFileId(fileEntity.getId());
        return response;
    }

    /**
     * 批量获取文件访问URL
     * 
     * @param fileIds 文件ID列表
     * @param expirySeconds URL过期时间（秒）
     * @return 文件ID到URL的映射
     */
    public Map<String, String> batchGetFileUrls(List<String> fileIds, Integer expirySeconds) {
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
            List<FileEntity> files = fileRepository.selectBatchIds(missedIds);
            
            for (FileEntity file : files) {
                // 验证文件状态
                if (file.getStatus() != FileStatus.COMPLETED || file.getDeletedAt() != null) {
                    log.warn("File is not available for URL generation: fileId={}, status={}", 
                            file.getId(), file.getStatus());
                    continue;
                }
                
                try {
                    // 生成预签名URL
                    String url = storageService.generatePresignedDownloadUrl(
                            file.getBucket(),
                            file.getStorageKey(),
                            expirySeconds
                    );
                    
                    result.put(file.getId(), url);
                    
                    // 缓存URL（缓存时间比URL过期时间短，避免返回即将失效的URL）
                    if (Boolean.TRUE.equals(mediaProperties.getUrl().getCacheEnabled())) {
                        String cacheKey = buildUrlCacheKey(file.getId());
                        int cacheTtl = Math.min(
                                mediaProperties.getUrl().getCacheTtl(),
                                expirySeconds - 3600 // 至少提前1小时过期
                        );
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
        
        return result;
    }

    /**
     * 构建URL缓存Key
     */
    private String buildUrlCacheKey(String fileId) {
        return "media:url:" + fileId;
    }

}
