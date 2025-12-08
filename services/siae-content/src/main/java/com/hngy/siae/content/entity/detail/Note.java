package com.hngy.siae.content.entity.detail;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 笔记详情表
 * @TableName note
 */
@Data
@TableName("note")
public class Note {
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
     * 笔记内容
     */
    private String content;

    /**
     * 笔记格式：markdown/rich_text
     */
    private String format;

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