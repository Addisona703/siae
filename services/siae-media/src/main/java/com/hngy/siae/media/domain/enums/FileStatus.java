package com.hngy.siae.media.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 文件状态枚举
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

    @EnumValue
    @JsonValue
    private final String value;
    private final String description;

    FileStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

}
