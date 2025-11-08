package com.hngy.siae.media.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 审计操作类型枚举
 *
 * @author SIAE Team
 */
@Getter
public enum AuditAction {
    
    INIT("init", "初始化上传"),
    COMPLETE("complete", "完成上传"),
    SIGN("sign", "生成签名"),
    DOWNLOAD("download", "下载文件"),
    DELETE("delete", "删除文件"),
    RESTORE("restore", "恢复文件"),
    UPDATE_ACL("update_acl", "更新权限"),
    GENERATE_PREVIEW("generate_preview", "生成预览");

    @EnumValue
    @JsonValue
    private final String value;
    private final String description;

    AuditAction(String value, String description) {
        this.value = value;
        this.description = description;
    }

}
