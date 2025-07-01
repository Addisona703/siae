package com.hngy.siae.content.entity.detail;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 视频详情表
 * @TableName content_video
 */
@Data
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
     * 视频访问URL
     */
    private String videoUrl;

    /**
     * 视频时长，单位：秒
     */
    private Integer duration;

    /**
     * 视频封面图URL
     */
    private String coverUrl;

    /**
     * 视频分辨率
     */
    private String resolution;

    /**
     * 播放次数
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