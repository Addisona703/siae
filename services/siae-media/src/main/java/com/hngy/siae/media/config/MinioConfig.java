package com.hngy.siae.media.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 配置类
 *
 * @author SIAE Team
 */
@Configuration
@ConfigurationProperties(prefix = "minio")
@Data
public class MinioConfig {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;
    private String region;
    private String externalEndpoint;

    /**
     * 内部操作客户端（用于上传、删除等实际操作）
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .region(region)
                .build();
    }

    /**
     * URL生成客户端（用于生成预签名URL，使用内部endpoint避免签名问题）
     */
    @Bean
    public MinioClient minioClientForUrl() {
        // 预签名URL必须使用内部endpoint（localhost）生成，避免签名验证失败
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .region(region)
                .build();
    }

}
