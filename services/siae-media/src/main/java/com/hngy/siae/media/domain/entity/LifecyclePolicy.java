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
 * 生命周期策略实体
 *
 * @author SIAE Team
 */
@Data
@TableName(value = "lifecycle_policies", autoResultMap = true)
public class LifecyclePolicy {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String tenantId;

    private String name;

    private String description;

    private Boolean enabled;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> rules;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
