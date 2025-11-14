package com.hngy.siae.content.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 收藏夹表
 * @TableName favorite_folder
 */
@Data
@TableName("favorite_folder")
public class FavoriteFolder {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 收藏夹名称
     */
    private String name;

    /**
     * 收藏夹描述
     */
    private String description;

    /**
     * 是否默认收藏夹（0否，1是）
     */
    private Integer isDefault;

    /**
     * 是否公开（0私密，1公开）
     */
    private Integer isPublic;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 收藏内容数量
     */
    private Integer itemCount;

    /**
     * 状态（0已删除，1正常）
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
