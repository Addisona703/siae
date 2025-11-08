package com.hngy.siae.media.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 租户配额实体
 *
 * @author SIAE Team
 */
@Data
@TableName(value = "quotas", autoResultMap = true)
public class Quota {

    @TableId
    private String tenantId;

    private Long bytesUsed;

    private Long objectsCount;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> limits;

    private String resetStrategy;

    private LocalDateTime updatedAt;

}
