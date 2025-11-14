package com.hngy.siae.content.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import com.hngy.siae.content.enums.status.CategoryStatusEnum;
import lombok.Data;

/**
 * 内容分类表
 * @TableName content_category
 */
@Data
public class Category {
    /**
     * 主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类编码
     */
    private String code;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 状态：0禁用，1启用，2已删除
     */
    private CategoryStatusEnum status;

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