package com.hngy.siae.media.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 处理任务类型枚举
 *
 * @author SIAE Team
 */
@Getter
public enum JobType {
    
    SCAN("scan", "病毒扫描"),
    THUMB("thumb", "缩略图生成"),
    OCR("ocr", "文字识别"),
    TRANSCODE("transcode", "转码"),
    PREVIEW("preview", "预览生成"),
    LIFECYCLE("lifecycle", "生命周期管理"),
    NOTIFY("notify", "通知");

    @EnumValue
    @JsonValue
    private final String value;
    private final String description;

    JobType(String value, String description) {
        this.value = value;
        this.description = description;
    }

}
