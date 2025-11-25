package com.hngy.siae.core.result;

import lombok.Getter;

/**
 * 媒体服务错误码定义
 *
 * 编码区间：3000-3099
 *
 * @author SIAE
 */
@Getter
public enum MediaResultCodeEnum implements IResultCode {

    FILE_NOT_FOUND(3001, "文件不存在"),
    FILE_ALREADY_DELETED(3002, "文件已删除"),
    FILE_NOT_DELETED(3003, "文件未删除"),
    FILE_STATUS_INVALID(3004, "文件状态不可用"),
    STORAGE_QUOTA_EXCEEDED(3005, "存储配额不足"),
    STORAGE_OBJECT_LIMIT_EXCEEDED(3006, "对象数量超限"),
    FILE_SIZE_EXCEEDS_LIMIT(3007, "文件大小超过限制"),
    FILE_TYPE_NOT_ALLOWED(3008, "不支持的文件类型"),
    UPLOAD_SESSION_NOT_FOUND(3009, "上传会话不存在"),
    UPLOAD_STATUS_INVALID(3010, "上传会话状态不正确"),
    UPLOAD_ALREADY_COMPLETED(3011, "上传已完成"),
    UPLOAD_PARTS_EMPTY(3012, "分片信息不能为空"),
    UPLOAD_PART_COUNT_MISMATCH(3013, "分片数量不匹配"),
    UPLOAD_FILE_SIZE_MISMATCH(3014, "文件大小不匹配"),
    STORAGE_OPERATION_FAILED(3015, "存储服务操作失败"),
    MEDIA_PROCESS_FAILED(3016, "媒体处理失败"),
    FILE_SCAN_FAILED(3017, "文件扫描失败"),
    TENANT_ID_MISSING(3018, "租户ID未设置"),
    USER_ID_MISSING(3019, "用户ID未设置"),
    UNAUTHORIZED_FILE_ACCESS(3020, "无权访问该文件"),
    STREAMING_TYPE_NOT_SUPPORTED(3021, "该文件类型不支持流式播放"),
    EVENT_PUBLISH_FAILED(3022, "事件发布失败"),
    FILE_DELETED(3023, "文件已被删除");

    private final int code;
    private final String message;

    MediaResultCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
