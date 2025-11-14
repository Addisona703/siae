package com.hngy.siae.content.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 收藏内容表
 * @TableName favorite_item
 */
@Data
@TableName("favorite_item")
public class FavoriteItem {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 收藏夹ID
     */
    private Long folderId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 内容ID
     */
    private Long contentId;

    /**
     * 收藏备注
     */
    private String note;

    /**
     * 在收藏夹内的排序
     */
    private Integer sortOrder;

    /**
     * 收藏时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
