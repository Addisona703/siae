package com.hngy.siae.media.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.hngy.siae.media.domain.enums.UploadStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 上传会话实体
 *
 * @author SIAE Team
 */
@Data
@TableName(value = "uploads", autoResultMap = true)
public class Upload {

    @TableId(type = IdType.ASSIGN_UUID)
    private String uploadId;

    private String fileId;

    private String tenantId;

    private Boolean multipart;

    private Integer partSize;

    private Integer totalParts;

    private Integer completedParts;

    private LocalDateTime expireAt;

    private UploadStatus status;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> callbacks;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
