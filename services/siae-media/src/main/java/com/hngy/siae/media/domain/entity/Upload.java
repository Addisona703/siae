package com.hngy.siae.media.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.hngy.siae.media.domain.enums.UploadStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 上传会话实体
 * 映射到 uploads 表，管理文件上传会话，支持断点续传
 * 
 * 每个上传会话关联一个文件记录（通过 fileId 外键）
 * 会话状态流转：init → in_progress → completed/expired/aborted
 * 
 * 支持分片上传：当 multipart=true 时，需要记录分片信息
 * 会话过期：当 expireAt 超过当前时间时，会话自动失效
 *
 * @author SIAE Team
 */
@Data
@TableName(value = "uploads", autoResultMap = true)
public class Upload {

    /**
     * 上传会话ID (UUID)
     * 主键，自动生成UUID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @TableField("upload_id")
    private String uploadId;

    /**
     * 关联的文件ID
     * 外键，关联到 files 表的 id 字段
     * 一个文件可以有多个上传会话（如重试、断点续传）
     */
    @TableField("file_id")
    private String fileId;

    /**
     * 租户ID
     * 用于多租户隔离
     */
    @TableField("tenant_id")
    private String tenantId;

    /**
     * 是否分片上传
     * true: 启用分片上传（大文件）
     * false: 单次上传（小文件）
     */
    @TableField("multipart")
    private Boolean multipart;

    /**
     * 分片大小（字节）
     * 仅在 multipart=true 时有效
     * 默认：10MB
     */
    @TableField("part_size")
    private Integer partSize;

    /**
     * 总分片数
     * 仅在 multipart=true 时有效
     * 计算公式：ceil(文件大小 / 分片大小)
     */
    @TableField("total_parts")
    private Integer totalParts;

    /**
     * 已完成分片数
     * 仅在 multipart=true 时有效
     * 用于跟踪上传进度
     */
    @TableField("completed_parts")
    private Integer completedParts;

    /**
     * 会话过期时间
     * 超过此时间后，会话自动失效
     * 默认：24小时后
     */
    @TableField("expire_at")
    private LocalDateTime expireAt;

    /**
     * 会话状态
     * init: 初始化
     * in_progress: 进行中
     * completed: 已完成
     * expired: 已过期
     * aborted: 已中止
     */
    @TableField("status")
    private UploadStatus status;

    /**
     * 回调配置（可选）
     * JSON对象，存储上传完成后的回调信息
     * 例如：{"url": "https://api.example.com/callback", "method": "POST"}
     */
    @TableField(value = "callbacks", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> callbacks;

    /**
     * 创建时间
     * 自动填充
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     * 自动填充
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

}
