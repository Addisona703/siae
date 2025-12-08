package com.hngy.siae.media.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 媒体服务配置属性
 *
 * @author SIAE Team
 */
@Configuration
@ConfigurationProperties(prefix = "media")
@Data
public class MediaProperties {

    private Upload upload = new Upload();
    private Download download = new Download();
    private Quota quota = new Quota();
    private Cdn cdn = new Cdn();
    private Url url = new Url();

    @Data
    public static class Upload {
        /**
         * 为避免外部配置缺失导致 NPE，这里提供与 application-dev.yaml 一致的默认值
         */
        private Long maxFileSize = 524_288_000L;        // 500 MB (单个文件最大大小，不启用分片时的限制)
        private Long maxMultipartSize = 2_147_483_648L; // 2 GB (分片上传总大小限制)
        private Long multipartThreshold = 10_485_760L;  // 10 MB (超过此大小启用分片上传)
        private Integer defaultPartSize = 10_485_760;   // 10 MB (默认分片大小)
        private Integer sessionExpiry = 86400;          // 24 小时 (上传会话过期时间，单位：秒)
        private Integer presignedUrlExpiry = 86400;     // 24 小时 (预签名URL过期时间，单位：秒)
        private List<String> allowedMimeTypes = List.of(
                "image/jpeg",
                "image/png",
                "image/gif",
                "application/pdf"
        );
    }

    @Data
    public static class Download {
        private Integer presignedUrlExpiry = 900;
    }

    @Data
    public static class Quota {
        private Long defaultMaxBytes = 10_737_418_240L;   // 10 GB
        private Integer defaultMaxObjects = 5_000;
    }

    @Data
    public static class Cdn {
        private Boolean enabled = false;
        private String baseUrl;
    }

    @Data
    public static class Url {
        /**
         * 公开文件URL缓存时间（秒），默认7天
         */
        private Integer publicTtl = 604800;
        
        /**
         * 私有文件URL缓存时间（秒），默认23小时
         */
        private Integer privateTtl = 82800;
        
        /**
         * URL默认过期时间（秒），默认24小时
         */
        private Integer expiration = 86400;
        
        /**
         * 是否启用缓存
         */
        private Boolean cacheEnabled = true;
    }

}
