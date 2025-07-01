package com.hngy.siae.content.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;

/**
 * 内容标签关系表
 * @TableName content_tag_relation
 */
@Data
@Builder
public class TagRelation {
    /**
     * 主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 内容ID
     */
    private Long contentId;

    /**
     * 标签ID
     */
    private Long tagId;

    /**
     * 关联创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}