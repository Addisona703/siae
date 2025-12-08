package com.hngy.siae.media.constant;

import com.hngy.siae.core.result.IResultCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Media 服务错误码枚举
 * 统一管理 Media 服务的所有错误码和错误信息
 * 
 * 错误码规范：
 * - 400: 请求参数无效、格式错误 (Requirements 9.1)
 * - 401: 未提供认证信息或认证失败 (Requirements 9.2)
 * - 403: 权限不足，无法访问资源 (Requirements 9.3)
 * - 404: 资源不存在 (Requirements 9.4)
 * - 502: 对象存储服务异常 (Requirements 9.5)
 * 
 * @author SIAE开发团队
 */
@Getter
@AllArgsConstructor
public enum MediaResultCode implements IResultCode {
    
    // ========== 文件相关错误 (404) ==========
    FILE_NOT_FOUND(404, "文件不存在"),
    
    // ========== 上传相关错误 (400, 404) ==========
    UPLOAD_NOT_FOUND(404, "上传会话不存在"),
    UPLOAD_EXPIRED(400, "上传会话已过期"),
    UPLOAD_ALREADY_COMPLETED(400, "上传已完成，无法重复操作"),
    UPLOAD_INVALID_STATUS(400, "上传会话状态无效"),
    UPLOAD_PART_MISMATCH(400, "分片信息不匹配"),
    INVALID_FILE_SIZE(400, "文件大小无效"),
    INVALID_FILE_NAME(400, "文件名无效"),
    INVALID_PARAMETERS(400, "请求参数无效"),
    
    // ========== 对象存储相关错误 (502) ==========
    STORAGE_SERVICE_ERROR(502, "对象存储服务异常"),
    STORAGE_UPLOAD_FAILED(502, "文件上传到存储服务失败"),
    STORAGE_DELETE_FAILED(502, "从存储服务删除文件失败"),
    STORAGE_URL_GENERATION_FAILED(502, "生成存储URL失败"),
    
    // ========== 权限相关错误 (401, 403) ==========
    UNAUTHORIZED(401, "未授权或认证失败"),
    ACCESS_DENIED(403, "无权访问该文件"),
    TENANT_MISMATCH(403, "租户不匹配"),
    
    // ========== 业务逻辑错误 (400) ==========
    INVALID_ACCESS_POLICY(400, "访问策略无效"),
    FILE_ALREADY_DELETED(400, "文件已被删除"),
    MULTIPART_NOT_SUPPORTED(400, "不支持分片上传");
    
    private final int code;
    private final String message;
    
    @Override
    public String getMessage() {
        return message;
    }
    
    @Override
    public Integer getCode() {
        return code;
    }
}
