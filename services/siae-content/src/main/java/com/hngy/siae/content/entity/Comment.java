package com.hngy.siae.content.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import com.hngy.siae.content.enums.status.CommentStatusEnum;
import lombok.Data;

/**
 * 内容评论表
 * @TableName content_comment
 */
@Data
@TableName("comment")
public class Comment {
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
     * 评论用户ID
     */
    private Long userId;

    /**
     * 父评论ID（顶级评论为NULL）
     */
    private Long parentId;

    /**
     * 回复目标用户ID（回复某人时使用）
     */
    private Long replyToUserId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 状态：0待审核，1已发布，2已删除
     */
    private CommentStatusEnum status;

    /**
     * 乐观锁版本号，用于防止并发审核冲突
     */
    @Version
    private Integer version;

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