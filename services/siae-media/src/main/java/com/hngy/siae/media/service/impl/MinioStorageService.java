package com.hngy.siae.media.service.impl;

import com.hngy.siae.media.config.MinioConfig;
import com.hngy.siae.media.service.StorageService;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * MinIO 存储服务实现
 * 
 * <p>实现与 MinIO 对象存储的所有交互操作：
 * <ul>
 *   <li>生成预签名 URL（上传/下载）</li>
 *   <li>生成公开访问 URL</li>
 *   <li>管理分片上传（初始化、生成分片 URL、完成、中断）</li>
 *   <li>删除对象</li>
 * </ul>
 * 
 * <p>使用两个 MinIO 客户端：
 * <ul>
 *   <li>minioClient: 用于实际操作（上传、删除等）</li>
 *   <li>minioClientForUrl: 用于生成外部可访问的 URL</li>
 * </ul>
 * 
 * @author SIAE Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;
    
    @Qualifier("minioClientForUrl")
    private final MinioClient minioClientForUrl;
    
    private final MinioConfig minioConfig;
    
    @Qualifier("urlGenerationExecutor")
    private final Executor urlGenerationExecutor;
    
    /**
     * AWS S3 客户端（用于真正的 Multipart Upload API）
     */
    private S3Client s3Client;
    
    /**
     * AWS S3 预签名器（用于生成分片上传的预签名 URL）
     */
    private S3Presigner s3Presigner;
    
    /**
     * 初始化 S3 客户端
     */
    @PostConstruct
    public void initS3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                minioConfig.getAccessKey(), 
                minioConfig.getSecretKey()
        );
        
        String region = minioConfig.getRegion() != null ? minioConfig.getRegion() : "us-east-1";
        
        // 创建 S3 客户端（用于 multipart upload 操作）
        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(minioConfig.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(region))
                .forcePathStyle(true)  // MinIO 需要 path-style
                .build();
        
        // 创建 S3 预签名器（用于生成预签名 URL）
        // 使用外部端点生成 URL，供客户端访问
        String presignerEndpoint = minioConfig.getExternalEndpoint() != null 
                && !minioConfig.getExternalEndpoint().isEmpty()
                ? minioConfig.getExternalEndpoint()
                : minioConfig.getEndpoint();
                
        this.s3Presigner = S3Presigner.builder()
                .endpointOverride(URI.create(presignerEndpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(region))
                .build();
        
        log.info("S3 client initialized for multipart upload: endpoint={}, presignerEndpoint={}", 
                minioConfig.getEndpoint(), presignerEndpoint);
    }

    /**
     * 生成预签名上传 URL
     * 
     * @param bucket 存储桶名称
     * @param objectKey 对象键（存储路径）
     * @param expirySeconds 过期时间（秒）
     * @return 预签名上传 URL
     */
    @Override
    public String generatePresignedUploadUrl(String bucket, String objectKey, int expirySeconds) {
        try {
            log.debug("Generating presigned upload URL: bucket={}, objectKey={}, expiry={}s", 
                     bucket, objectKey, expirySeconds);
            
            String url = minioClientForUrl.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(bucket)
                    .object(objectKey)
                    .expiry(expirySeconds, TimeUnit.SECONDS)
                    .build()
            );
            
            log.debug("Generated presigned upload URL: {}", url);
            return url;
            
        } catch (Exception e) {
            log.error("Failed to generate presigned upload URL: bucket={}, objectKey={}", 
                     bucket, objectKey, e);
            throw new StorageException("Failed to generate presigned upload URL", e);
        }
    }

    /**
     * 生成预签名下载 URL
     * 用于私有文件的临时访问
     * 
     * @param bucket 存储桶名称
     * @param objectKey 对象键（存储路径）
     * @param expirySeconds 过期时间（秒）
     * @return 预签名下载 URL
     */
    @Override
    public String generatePresignedDownloadUrl(String bucket, String objectKey, int expirySeconds) {
        try {
            log.debug("Generating presigned download URL: bucket={}, objectKey={}, expiry={}s", 
                     bucket, objectKey, expirySeconds);
            
            String url = minioClientForUrl.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(objectKey)
                    .expiry(expirySeconds, TimeUnit.SECONDS)
                    .build()
            );
            
            log.debug("Generated presigned download URL: {}", url);
            return url;
            
        } catch (Exception e) {
            log.error("Failed to generate presigned download URL: bucket={}, objectKey={}", 
                     bucket, objectKey, e);
            throw new StorageException("Failed to generate presigned download URL", e);
        }
    }

    /**
     * 生成公开访问 URL
     * 用于公开文件的永久访问，不带签名
     * 
     * @param bucket 存储桶名称
     * @param objectKey 对象键（存储路径）
     * @return 公开访问 URL（永久有效）
     */
    @Override
    public String generatePublicUrl(String bucket, String objectKey) {
        try {
            log.debug("Generating public URL: bucket={}, objectKey={}", bucket, objectKey);
            
            // 公开URL使用外部端点（供局域网访问）
            String endpoint = minioConfig.getExternalEndpoint() != null && !minioConfig.getExternalEndpoint().isEmpty()
                    ? minioConfig.getExternalEndpoint()
                    : minioConfig.getEndpoint();
            
            // 确保端点不以 / 结尾
            if (endpoint.endsWith("/")) {
                endpoint = endpoint.substring(0, endpoint.length() - 1);
            }
            
            // 构建公开 URL: {endpoint}/{bucket}/{objectKey}
            String publicUrl = endpoint + "/" + bucket + "/" + objectKey;
            
            log.debug("Generated public URL: {}", publicUrl);
            return publicUrl;
            
        } catch (Exception e) {
            log.error("Failed to generate public URL: bucket={}, objectKey={}", bucket, objectKey, e);
            throw new StorageException("Failed to generate public URL", e);
        }
    }

    /**
     * 初始化分片上传
     * 调用 S3 CreateMultipartUpload API 获取真正的 uploadId
     * 
     * @param bucket 存储桶名称
     * @param objectKey 对象键（存储路径）
     * @return 上传会话 ID（uploadId）
     */
    @Override
    public String initMultipartUpload(String bucket, String objectKey) {
        try {
            log.debug("Initializing multipart upload: bucket={}, objectKey={}", bucket, objectKey);
            
            // 调用 S3 API 创建真正的 multipart upload
            software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest request = 
                    software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();
            
            software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse response = 
                    s3Client.createMultipartUpload(request);
            String uploadId = response.uploadId();
            
            log.info("Created multipart upload: bucket={}, objectKey={}, uploadId={}", 
                    bucket, objectKey, uploadId);
            
            return uploadId;
            
        } catch (Exception e) {
            log.error("Failed to initialize multipart upload: bucket={}, objectKey={}", 
                     bucket, objectKey, e);
            throw new StorageException("Failed to initialize multipart upload", e);
        }
    }

    /**
     * 生成分片上传预签名 URL
     * 使用 S3 Presigner 为每个分片生成独立的上传 URL
     * 
     * @param bucket 存储桶名称
     * @param objectKey 对象键（存储路径）
     * @param uploadId 上传会话 ID
     * @param partNumber 分片编号（从 1 开始）
     * @param expirySeconds 过期时间（秒）
     * @return 分片上传预签名 URL
     */
    @Override
    public String generatePresignedPartUploadUrl(String bucket, String objectKey, String uploadId, 
                                                 int partNumber, int expirySeconds) {
        try {
            log.debug("Generating presigned part upload URL: bucket={}, objectKey={}, uploadId={}, partNumber={}, expiry={}s",
                     bucket, objectKey, uploadId, partNumber, expirySeconds);
            
            // 使用 S3 Presigner 生成分片上传的预签名 URL
            UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .uploadId(uploadId)
                    .partNumber(partNumber)
                    .build();
            
            UploadPartPresignRequest presignRequest = UploadPartPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(expirySeconds))
                    .uploadPartRequest(uploadPartRequest)
                    .build();
            
            PresignedUploadPartRequest presignedRequest = s3Presigner.presignUploadPart(presignRequest);
            String url = presignedRequest.url().toString();
            
            log.debug("Generated presigned part upload URL for part {}: {}", partNumber, url);
            return url;
            
        } catch (Exception e) {
            log.error("Failed to generate presigned part upload URL: bucket={}, objectKey={}, uploadId={}, partNumber={}", 
                     bucket, objectKey, uploadId, partNumber, e);
            throw new StorageException("Failed to generate presigned part upload URL", e);
        }
    }

    /**
     * 批量生成分片上传预签名 URL（并行生成，提升性能）
     * 使用 S3 Presigner 生成真正的分片上传 URL
     * 
     * @param bucket 存储桶名称
     * @param objectKey 对象键（存储路径）
     * @param uploadId 上传会话 ID
     * @param totalParts 总分片数
     * @param expirySeconds 过期时间（秒）
     * @return 分片上传预签名 URL 列表（按分片编号排序）
     */
    @Override
    public List<String> batchGeneratePresignedPartUploadUrls(String bucket, String objectKey, String uploadId,
                                                              int totalParts, int expirySeconds) {
        log.debug("Batch generating presigned part upload URLs: bucket={}, objectKey={}, uploadId={}, totalParts={}, expiry={}s",
                bucket, objectKey, uploadId, totalParts, expirySeconds);
        
        long startTime = System.currentTimeMillis();
        
        // 并行生成所有分片的预签名 URL
        List<CompletableFuture<String>> futures = IntStream.rangeClosed(1, totalParts)
                .mapToObj(partNumber -> CompletableFuture.supplyAsync(() -> {
                    try {
                        UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                                .bucket(bucket)
                                .key(objectKey)
                                .uploadId(uploadId)
                                .partNumber(partNumber)
                                .build();
                        
                        UploadPartPresignRequest presignRequest = UploadPartPresignRequest.builder()
                                .signatureDuration(Duration.ofSeconds(expirySeconds))
                                .uploadPartRequest(uploadPartRequest)
                                .build();
                        
                        return s3Presigner.presignUploadPart(presignRequest).url().toString();
                    } catch (Exception e) {
                        log.error("Failed to generate presigned URL for part {}: bucket={}, objectKey={}, uploadId={}",
                                partNumber, bucket, objectKey, uploadId, e);
                        throw new StorageException("Failed to generate presigned URL for part " + partNumber, e);
                    }
                }, urlGenerationExecutor))
                .toList();
        
        // 等待所有 URL 生成完成
        List<String> urls = futures.stream()
                .map(CompletableFuture::join)
                .toList();
        
        long elapsed = System.currentTimeMillis() - startTime;
        log.info("Batch generated {} presigned part upload URLs in {}ms: bucket={}, objectKey={}, uploadId={}",
                totalParts, elapsed, bucket, objectKey, uploadId);
        
        return urls;
    }

    /**
     * 完成分片上传
     * 调用 S3 CompleteMultipartUpload API 合并所有分片为完整文件
     * 
     * @param bucket 存储桶名称
     * @param objectKey 对象键（存储路径）
     * @param uploadId 上传会话 ID
     * @param parts 分片信息列表（包含 partNumber 和 ETag）
     */
    @Override
    public void completeMultipartUpload(String bucket, String objectKey, String uploadId, List<PartETag> parts) {
        try {
            log.debug("Completing multipart upload: bucket={}, objectKey={}, uploadId={}, parts count={}", 
                     bucket, objectKey, uploadId, parts.size());
            
            // 构建 CompletedPart 列表
            List<CompletedPart> completedParts = parts.stream()
                    .map(p -> CompletedPart.builder()
                            .partNumber(p.partNumber())
                            .eTag(p.etag())
                            .build())
                    .toList();
            
            // 调用 S3 API 完成分片上传
            CompleteMultipartUploadRequest request = CompleteMultipartUploadRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .uploadId(uploadId)
                    .multipartUpload(CompletedMultipartUpload.builder()
                            .parts(completedParts)
                            .build())
                    .build();
            
            CompleteMultipartUploadResponse response = s3Client.completeMultipartUpload(request);
            
            log.info("Multipart upload completed: bucket={}, objectKey={}, uploadId={}, parts={}, etag={}", 
                    bucket, objectKey, uploadId, parts.size(), response.eTag());
            
        } catch (Exception e) {
            log.error("Failed to complete multipart upload: bucket={}, objectKey={}, uploadId={}", 
                     bucket, objectKey, uploadId, e);
            throw new StorageException("Failed to complete multipart upload", e);
        }
    }

    /**
     * 中断分片上传
     * 调用 S3 AbortMultipartUpload API 清理已上传的分片
     * 
     * @param bucket 存储桶名称
     * @param objectKey 对象键（存储路径）
     * @param uploadId 上传会话 ID
     */
    @Override
    public void abortMultipartUpload(String bucket, String objectKey, String uploadId) {
        try {
            log.debug("Aborting multipart upload: bucket={}, objectKey={}, uploadId={}", 
                     bucket, objectKey, uploadId);
            
            // 调用 S3 API 中断分片上传
            AbortMultipartUploadRequest request = AbortMultipartUploadRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .uploadId(uploadId)
                    .build();
            
            s3Client.abortMultipartUpload(request);
            
            log.info("Multipart upload aborted: bucket={}, objectKey={}, uploadId={}", 
                    bucket, objectKey, uploadId);
            
        } catch (Exception e) {
            log.error("Failed to abort multipart upload: bucket={}, objectKey={}, uploadId={}", 
                     bucket, objectKey, uploadId, e);
            throw new StorageException("Failed to abort multipart upload", e);
        }
    }

    /**
     * 删除对象
     * 从对象存储中删除文件
     * 
     * @param bucket 存储桶名称
     * @param objectKey 对象键（存储路径）
     */
    @Override
    public void deleteObject(String bucket, String objectKey) {
        try {
            log.debug("Deleting object: bucket={}, objectKey={}", bucket, objectKey);
            
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build()
            );
            
            log.info("Deleted object: bucket={}, objectKey={}", bucket, objectKey);
            
        } catch (Exception e) {
            log.error("Failed to delete object: bucket={}, objectKey={}", bucket, objectKey, e);
            throw new StorageException("Failed to delete object", e);
        }
    }

    /**
     * 获取文件字节数据
     * 从对象存储中读取文件内容
     * 
     * @param bucket 存储桶名称
     * @param objectKey 对象键（存储路径）
     * @return 文件字节数组
     */
    @Override
    public byte[] getObjectBytes(String bucket, String objectKey) {
        try {
            log.debug("Getting object bytes: bucket={}, objectKey={}", bucket, objectKey);
            
            try (var stream = minioClient.getObject(
                    io.minio.GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .build())) {
                
                byte[] bytes = stream.readAllBytes();
                log.info("Retrieved object bytes: bucket={}, objectKey={}, size={} bytes", 
                        bucket, objectKey, bytes.length);
                return bytes;
            }
            
        } catch (Exception e) {
            log.error("Failed to get object bytes: bucket={}, objectKey={}", bucket, objectKey, e);
            throw new StorageException("Failed to get object bytes", e);
        }
    }

    /**
     * 存储异常
     * 封装所有与对象存储交互相关的异常
     */
    public static class StorageException extends RuntimeException {
        public StorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
