package com.hngy.siae.content.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import com.hngy.siae.content.enums.ContentTypeEnum;
import com.hngy.siae.content.enums.status.ContentStatusEnum;
import lombok.Data;

@Data
@TableName("content")
public class Content {
    /**
     * 主键，自增，方便回填
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 资源标题
     */
    private String title;

    /**
     * 资源类型（article、note、question、file、video）
     */
    private ContentTypeEnum type;

    /**
     * 资源摘要，用于列表页或预览页展示
     */
    private String description;

    /**
     * 封面文件ID，关联media服务（UUID字符串）
     */
    private String coverFileId;

    /**
     * 上传者/作者用户 ID
     */
    private Long uploadedBy;

    /**
     * 状态：0草稿，1待审核，2已发布，3已删除
     */
    private ContentStatusEnum status;

    /**
     * 乐观锁版本号，用于防止并发审核冲突
     */
    @Version
    private Integer version;

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

    /**
     * 关联的分类ID，外键，指向 content_category 表
     */
    private Long categoryId;
}