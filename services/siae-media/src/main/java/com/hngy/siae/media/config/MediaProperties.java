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
        private Long maxFileSize = 104_857_600L;        // 100 MB
        private Long maxMultipartSize = 536_870_912L;   // 512 MB
        private Integer presignedUrlExpiry = 900;
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
         * URL默认过期时间（秒），默认24小时
         */
        private Integer expiration = 86400;
        
        /**
         * 是否启用缓存
         */
        private Boolean cacheEnabled = true;
        
        /**
         * 缓存TTL（秒），默认23小时（比URL过期时间短1小时）
         */
        private Integer cacheTtl = 82800;
    }

}
