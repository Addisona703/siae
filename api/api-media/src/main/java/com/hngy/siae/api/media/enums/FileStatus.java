package com.hngy.siae.api.media.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 文件状态枚举
 * 与 siae-media 服务的 FileStatus 保持一致
 *
 * @author SIAE Team
 */
@Getter
public enum FileStatus {
    
    INIT("init", "初始化"),
    UPLOADING("uploading", "上传中"),
    PROCESSING("processing", "处理中"),
    COMPLETED("completed", "已完成"),
    FAILED("failed", "失败"),
    DELETED("deleted", "已删除");

    @JsonValue
    private final String value;
    
    private final String description;

    FileStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
