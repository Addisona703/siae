package com.hngy.siae.content.entity.detail;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 文件详情表（精简版）
 * 移除了 fileName、fileSize、fileType 字段，这些信息通过 Media 服务获取
 * 
 * @TableName file
 * @see com.hngy.siae.api.media.client.MediaFeignClient
 */
@Data
@TableName("file")
public class File {
    /**
     * 主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的内容ID，外键，指向 content 表
     */
    private Long contentId;

    /**
     * 文件ID（UUID字符串），关联 media 服务
     * 通过此 ID 调用 MediaFeignClient 获取文件元数据（文件名、大小、类型等）
     */
    private String fileId;

    /**
     * 下载次数（业务统计）
     */
    private Integer downloadCount = 0;

    /**
     * 创建时间，默认当前时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间，默认当前时间，更新时自动刷新
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}