package com.hngy.siae.media.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 处理任务状态枚举
 *
 * @author SIAE Team
 */
@Getter
public enum JobStatus {
    
    PENDING("pending", "待处理"),
    RUNNING("running", "运行中"),
    SUCCESS("success", "成功"),
    FAILED("failed", "失败"),
    DEAD_LETTER("dead_letter", "死信");

    @EnumValue
    @JsonValue
    private final String value;
    private final String description;

    JobStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

}
