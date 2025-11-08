package com.hngy.siae.media.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 文件衍生物实体
 * 存储缩略图、转码结果等
 *
 * @author SIAE Team
 */
@Data
@TableName(value = "file_derivatives", autoResultMap = true)
public class FileDerivative {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String fileId;

    private String type;

    private String storageKey;

    private Long size;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> metadata;

    private LocalDateTime createdAt;

}
