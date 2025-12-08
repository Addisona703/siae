package com.hngy.siae.media.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketLifecycleArgs;
import io.minio.SetBucketPolicyArgs;
import io.minio.messages.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

/**
 * MinIO 初始化器
 * 在应用启动时自动创建必要的 Buckets 和配置生命周期策略
 *
 * @author SIAE Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioInitializer implements ApplicationRunner {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    @Override
    public void run(ApplicationArguments args) {
        try {
            log.info("开始初始化 MinIO Buckets...");
            
            // 1. 创建主存储桶
            createBucketIfNotExists(minioConfig.getBucketName(), "主存储桶 - 存储用户上传的文件");
            
            // 2. 创建衍生文件存储桶（可选）
            String derivativesBucket = minioConfig.getBucketName() + "-derivatives";
            createBucketIfNotExists(derivativesBucket, "衍生文件存储桶 - 存储缩略图、转码文件等");
            
            // 3. 创建临时文件存储桶（可选）
            String tempBucket = minioConfig.getBucketName() + "-temp";
            createBucketIfNotExists(tempBucket, "临时文件存储桶 - 存储分片上传的临时文件");
            
            // 4. 配置临时文件桶的生命周期策略（7天后自动删除）
            setTempBucketLifecycle(tempBucket);
            
            // 5. 配置主存储桶的公开访问策略
            setPublicAccessPolicy(minioConfig.getBucketName());
            
            log.info("MinIO Buckets 初始化完成");
            
        } catch (Exception e) {
            log.error("MinIO 初始化失败", e);
            // 不抛出异常，允许应用继续启动
        }
    }

    /**
     * 创建 Bucket（如果不存在）
     */
    private void createBucketIfNotExists(String bucketName, String description) {
        try {
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build()
            );
            
            if (!exists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build()
                );
                log.info("✓ 创建 Bucket 成功: {} - {}", bucketName, description);
            } else {
                log.info("✓ Bucket 已存在: {}", bucketName);
            }
            
        } catch (Exception e) {
            log.error("创建 Bucket 失败: {}", bucketName, e);
        }
    }

    /**
     * 设置临时文件桶的生命周期策略
     * 自动删除 7 天前的文件
     */
    private void setTempBucketLifecycle(String bucketName) {
        try {
            // 创建生命周期规则
            List<LifecycleRule> rules = new LinkedList<>();
            
            // 规则：7天后删除所有文件
            LifecycleRule rule = new LifecycleRule(
                Status.ENABLED,                                  // 状态：启用
                null,                                            // AbortIncompleteMultipartUpload
                new Expiration((ZonedDateTime) null, 7, null),  // 7天后过期
                new RuleFilter(""),                              // 应用到所有对象
                "temp-cleanup-rule",                             // 规则ID
                null,                                            // NoncurrentVersionExpiration
                null,                                            // NoncurrentVersionTransition
                null                                             // Transition
            );
            
            rules.add(rule);
            
            // 创建生命周期配置
            LifecycleConfiguration config = new LifecycleConfiguration(rules);
            
            // 应用生命周期配置
            minioClient.setBucketLifecycle(
                SetBucketLifecycleArgs.builder()
                    .bucket(bucketName)
                    .config(config)
                    .build()
            );
            
            log.info("✓ 设置 Bucket 生命周期策略成功: {} (7天后自动删除)", bucketName);
            
        } catch (Exception e) {
            log.warn("设置 Bucket 生命周期策略失败: {} - {}", bucketName, e.getMessage());
        }
    }

    /**
     * 设置公开访问策略
     * 允许匿名访问 public 路径下的文件
     */
    private void setPublicAccessPolicy(String bucketName) {
        try {
            // 创建策略 JSON
            String policy = """
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": {"AWS": ["*"]},
                      "Action": ["s3:GetObject"],
                      "Resource": ["arn:aws:s3:::%s/*/public/*"]
                    }
                  ]
                }
                """.formatted(bucketName);
            
            // 应用策略
            minioClient.setBucketPolicy(
                SetBucketPolicyArgs.builder()
                    .bucket(bucketName)
                    .config(policy)
                    .build()
            );
            
            log.info("✓ 设置公开访问策略成功: {} (允许匿名访问 */public/* 路径)", bucketName);
            
        } catch (Exception e) {
            log.warn("设置公开访问策略失败: {} - {}", bucketName, e.getMessage());
        }
    }

}
