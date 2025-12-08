package com.hngy.siae.media.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.hngy.siae.media.domain.enums.AccessPolicy;
import com.hngy.siae.media.domain.enums.FileStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 文件实体
 * 映射到 files 表，存储文件的元数据信息
 * 
 * 支持软删除：当 deletedAt 不为 null 时，表示文件已被删除
 * 查询时会自动过滤已删除的文件（通过 @TableLogic 注解）
 *
 * @author SIAE Team
 */
@Data
@TableName(value = "files", autoResultMap = true)
public class FileEntity {

    /**
     * 文件ID (UUID)
     * 主键，自动生成UUID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 租户ID
     * 用于多租户隔离
     */
    @TableField("tenant_id")
    private String tenantId;

    /**
     * 所有者ID
     * 文件的拥有者用户ID
     */
    @TableField("owner_id")
    private String ownerId;

    /**
     * 文件名
     * 原始文件名，用于显示和下载
     */
    @TableField("filename")
    private String filename;

    /**
     * 文件大小（字节）
     */
    @TableField("size")
    private Long size;

    /**
     * MIME类型
     * 例如：image/jpeg, application/pdf
     */
    @TableField("mime")
    private String mime;

    /**
     * 存储桶名称
     * MinIO bucket name
     */
    @TableField("bucket")
    private String bucket;

    /**
     * 对象存储键
     * 文件在对象存储中的完整路径
     * 格式：{tenant-id}/{public|private}/{timestamp}/{filename}
     */
    @TableField("storage_key")
    private String storageKey;

    /**
     * 访问策略
     * PUBLIC: 公开访问，生成永久URL
     * PRIVATE: 私有访问，生成临时签名URL
     */
    @TableField("access_policy")
    private AccessPolicy accessPolicy;

    /**
     * 文件状态
     * init: 初始化
     * uploading: 上传中
     * completed: 已完成
     * failed: 失败
     */
    @TableField("status")
    private FileStatus status;

    /**
     * 业务标签
     * JSON数组，用于文件分类和查询
     * 例如：["avatar", "profile"]
     */
    @TableField(value = "biz_tags", typeHandler = JacksonTypeHandler.class)
    private List<String> bizTags;

    /**
     * 扩展属性
     * JSON对象，存储业务自定义字段
     */
    @TableField(value = "ext", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> ext;

    /**
     * SHA256校验和
     * 用于文件完整性验证和去重
     */
    @TableField("sha256")
    private String sha256;

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

    /**
     * 删除时间（软删除标记）
     * 当此字段不为null时，表示文件已被删除
     * 使用 @TableLogic 注解实现软删除：
     * - 查询时自动过滤 deletedAt IS NOT NULL 的记录
     * - 删除时自动设置 deletedAt = NOW()
     */
    @TableLogic
    @TableField("deleted_at")
    private LocalDateTime deletedAt;

}
