package com.hngy.siae.media.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.hngy.siae.media.domain.enums.JobStatus;
import com.hngy.siae.media.domain.enums.JobType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 异步处理任务实体
 *
 * @author SIAE Team
 */
@Data
@TableName(value = "processing_jobs", autoResultMap = true)
public class ProcessingJob {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String fileId;

    private JobType jobType;

    private JobStatus status;

    private Integer priority;

    private Integer attempts;

    private String lastError;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> payload;

    private LocalDateTime scheduledAt;

    private LocalDateTime updatedAt;

}
