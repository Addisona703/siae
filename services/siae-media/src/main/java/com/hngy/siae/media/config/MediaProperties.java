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

}
