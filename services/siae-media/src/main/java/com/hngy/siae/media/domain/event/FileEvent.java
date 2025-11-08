package com.hngy.siae.media.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 文件事件基类
 *
 * @author SIAE Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileEvent {

    /**
     * 事件ID
     */
    private String eventId;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 文件ID
     */
    private String fileId;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 文件信息
     */
    private FileInfo fileInfo;

    /**
     * 事件时间
     */
    private LocalDateTime timestamp;

    /**
     * 扩展数据
     */
    private Map<String, Object> metadata;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileInfo {
        private String bucket;
        private String storageKey;
        private Long size;
        private String mime;
        private String sha256;
        private String ownerId;
    }

}
