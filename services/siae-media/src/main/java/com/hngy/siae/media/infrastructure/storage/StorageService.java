package com.hngy.siae.media.infrastructure.storage;

import cn.hutool.core.util.StrUtil;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.MediaResultCodeEnum;
import com.hngy.siae.media.config.MediaProperties;
import com.hngy.siae.media.config.MinioConfig;
import io.minio.BucketExistsArgs;
import io.minio.ComposeObjectArgs;
import io.minio.ComposeSource;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 对象存储服务
 * 封装 MinIO 操作
 *
 * @author SIAE Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final MinioClient minioClient;  // 用于实际操作
    private final MinioClient minioClientForUrl;  // 用于生成URL
    private final MinioConfig minioConfig;
    private final MediaProperties mediaProperties;
    
    @Qualifier("urlGenerationExecutor")
    private final Executor urlGenerationExecutor;

    /**
     * 确保存储桶存在
     */
    public void ensureBucketExists(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("Created bucket: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("Failed to ensure bucket exists: {}", bucketName, e);
            AssertUtils.fail(MediaResultCodeEnum.STORAGE_OPERATION_FAILED);
        }
    }

    /**
     * 生成预签名上传 URL
     */
    public String generatePresignedUploadUrl(String bucketName, String objectKey, int expirySeconds) {
        try {
            return minioClientForUrl.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(objectKey)
                            .expiry(expirySeconds, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to generate presigned upload URL for {}/{}", bucketName, objectKey, e);
            AssertUtils.fail(MediaResultCodeEnum.STORAGE_OPERATION_FAILED);
            return null;
        }
    }

    /**
     * 生成预签名下载 URL
     */
    public String generatePresignedDownloadUrl(String bucketName, String objectKey, int expirySeconds) {
        try {
            String url = minioClientForUrl.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectKey)
                            .expiry(expirySeconds, TimeUnit.SECONDS)
                            .build()
            );
            return applyCdnIfEnabled(url);
        } catch (Exception e) {
            log.error("Failed to generate presigned download URL for {}/{}", bucketName, objectKey, e);
            AssertUtils.fail(MediaResultCodeEnum.STORAGE_OPERATION_FAILED);
            return null;
        }
    }

    /**
     * 初始化分片上传
     */
    public String initiateMultipartUpload(String bucketName, String objectKey) {
        try {
            // MinIO SDK 不直接暴露 initiateMultipartUpload，使用预签名 URL 方式
            return generatePresignedUploadUrl(bucketName, objectKey, 86400);
        } catch (Exception e) {
            log.error("Failed to initiate multipart upload for {}/{}", bucketName, objectKey, e);
            AssertUtils.fail(MediaResultCodeEnum.STORAGE_OPERATION_FAILED);
            return null;
        }
    }

    /**
     * 生成分片上传 URL
     */
    public String generatePartUploadUrl(String bucketName, String objectKey, int partNumber, int expirySeconds) {
        try {
            String partObjectKey = buildPartObjectKey(objectKey, partNumber);
            return minioClientForUrl.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(partObjectKey)
                            .expiry(expirySeconds, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to generate part upload URL for {}/{} part {}", bucketName, objectKey, partNumber, e);
            AssertUtils.fail(MediaResultCodeEnum.STORAGE_OPERATION_FAILED);
            return null;
        }
    }

    /**
     * 批量生成分片上传 URL（并行优化）
     * 
     * @param bucketName 存储桶名称
     * @param objectKey 对象键
     * @param totalParts 总分片数
     * @param expirySeconds 过期时间（秒）
     * @return 预签名URL列表
     */
    public List<String> batchGeneratePartUploadUrls(String bucketName, String objectKey, 
                                                     int totalParts, int expirySeconds) {
        log.info("Batch generating {} part upload URLs for {}/{}", totalParts, bucketName, objectKey);
        long startTime = System.currentTimeMillis();
        
        try {
            List<CompletableFuture<String>> futures = new ArrayList<>(totalParts);
            
            // 并行生成所有分片的预签名URL
            for (int i = 1; i <= totalParts; i++) {
                final int partNumber = i;
                CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        String partObjectKey = buildPartObjectKey(objectKey, partNumber);
                        return minioClientForUrl.getPresignedObjectUrl(
                                GetPresignedObjectUrlArgs.builder()
                                        .method(Method.PUT)
                                        .bucket(bucketName)
                                        .object(partObjectKey)
                                        .expiry(expirySeconds, TimeUnit.SECONDS)
                                        .build()
                        );
                    } catch (Exception e) {
                        log.error("Failed to generate part upload URL for part {}", partNumber, e);
                        throw new RuntimeException("Failed to generate URL for part " + partNumber, e);
                    }
                }, urlGenerationExecutor);
                
                futures.add(future);
            }
            
            // 等待所有URL生成完成
            List<String> urls = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("Batch generated {} URLs in {}ms (avg: {}ms per URL)", 
                    totalParts, duration, duration / totalParts);
            
            return urls;
            
        } catch (Exception e) {
            log.error("Failed to batch generate part upload URLs", e);
            AssertUtils.fail(MediaResultCodeEnum.STORAGE_OPERATION_FAILED);
            return null;
        }
    }

    /**
     * 删除对象
     */
    public void deleteObject(String bucketName, String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );
            log.info("Deleted object: {}/{}", bucketName, objectKey);
        } catch (Exception e) {
            log.error("Failed to delete object {}/{}", bucketName, objectKey, e);
            AssertUtils.fail(MediaResultCodeEnum.STORAGE_OPERATION_FAILED);
        }
    }

    /**
     * 获取对象流
     */
    public GetObjectResponse getObject(String bucketName, String objectKey) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to get object {}/{}", bucketName, objectKey, e);
            AssertUtils.fail(MediaResultCodeEnum.STORAGE_OPERATION_FAILED);
            return null;
        }
    }

    public void composeObject(String bucketName, List<String> partKeys, String targetKey) {
        try {
            List<ComposeSource> sources = partKeys.stream()
                    .map(key -> ComposeSource.builder().bucket(bucketName).object(key).build())
                    .toList();
            minioClient.composeObject(
                    ComposeObjectArgs.builder()
                            .bucket(bucketName)
                            .object(targetKey)
                            .sources(sources)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to compose object {} from parts", targetKey, e);
            AssertUtils.fail(MediaResultCodeEnum.STORAGE_OPERATION_FAILED);
        }
    }

    public void deleteObjects(String bucketName, List<String> objectKeys) {
        for (String key : objectKeys) {
            deleteObject(bucketName, key);
        }
    }

    /**
     * 检查对象是否存在
     */
    public boolean objectExists(String bucketName, String objectKey) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String buildPartObjectKey(String objectKey, int partNumber) {
        return String.format("%s.__part__%05d", objectKey, partNumber);
    }

    /**
     * 获取默认存储桶名称
     */
    public String getDefaultBucket() {
        return minioConfig.getBucketName();
    }

    /**
     * 根据租户ID获取存储桶名称
     */
    public String getTenantBucket(String tenantId) {
        // 可以为每个租户创建独立的桶，或使用统一桶+前缀
        return minioConfig.getBucketName();
    }

    /**
     * 检查存储服务是否可用
     */
    public boolean isAvailable() {
        try {
            // 尝试列出存储桶来检查连接
            minioClient.listBuckets();
            return true;
        } catch (Exception e) {
            log.error("Storage service is not available", e);
            return false;
        }
    }

    public String applyCdnIfEnabled(String originalUrl) {
        MediaProperties.Cdn cdn = mediaProperties.getCdn();
        if (cdn == null || !Boolean.TRUE.equals(cdn.getEnabled()) || StrUtil.isBlank(cdn.getBaseUrl())) {
            return originalUrl;
        }
        try {
            java.net.URI source = java.net.URI.create(originalUrl);
            java.net.URI cdnBase = java.net.URI.create(cdn.getBaseUrl());
            String scheme = StrUtil.blankToDefault(cdnBase.getScheme(), source.getScheme());
            java.net.URI rewritten = new java.net.URI(
                    scheme,
                    cdnBase.getAuthority(),
                    source.getPath(),
                    source.getQuery(),
                    source.getFragment()
            );
            return rewritten.toString();
        } catch (Exception e) {
            log.warn("Failed to rewrite CDN url. base={}, url={}, reason={}", cdn.getBaseUrl(), originalUrl, e.getMessage());
            return originalUrl;
        }
    }

}
