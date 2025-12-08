package com.hngy.siae.content.entity.detail;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 视频详情表
 * 
 * @TableName video
 * @see com.hngy.siae.api.media.client.MediaFeignClient
 */
@Data
@TableName("video")
public class Video {
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
     * 视频文件ID（UUID字符串），关联 media 服务
     * 通过此 ID 调用 MediaFeignClient 获取视频元数据（时长、分辨率等）
     */
    private String videoFileId;

    /**
     * 播放次数（业务统计）
     */
    private Integer playCount = 0;

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