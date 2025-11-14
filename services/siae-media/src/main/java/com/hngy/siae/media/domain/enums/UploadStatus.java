package com.hngy.siae.media.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 上传状态枚举
 *
 * @author SIAE Team
 */
@Getter
public enum UploadStatus {
    
    INIT("init", "初始化"),
    IN_PROGRESS("in_progress", "进行中"),
    PROCESSING("processing", "处理中"),
    COMPLETED("completed", "已完成"),
    FAILED("failed", "失败"),
    EXPIRED("expired", "已过期"),
    ABORTED("aborted", "已中止");

    @EnumValue
    @JsonValue
    private final String value;
    private final String description;

    UploadStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

}
