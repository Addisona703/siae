package com.hngy.siae.media.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.hngy.siae.media.domain.enums.FileStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 文件实体
 *
 * @author SIAE Team
 */
@Data
@TableName(value = "files", autoResultMap = true)
public class FileEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String tenantId;

    private String ownerId;

    private String bucket;

    private String storageKey;

    private Long size;

    private String mime;

    private String sha256;

    private FileStatus status;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> acl;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private java.util.List<String> bizTags;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> ext;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> checksum;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

}
