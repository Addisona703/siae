package com.hngy.siae.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.hngy.siae.notification.enums.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统通知实体类
 *
 * @author KEYKB
 */
@Data
@TableName("system_notification")
public class SystemNotification {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 通知类型：1=系统通知,2=公告,3=提醒
     */
    private NotificationType type;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 跳转链接
     */
    private String linkUrl;

    /**
     * 是否已读：false=未读,true=已读
     */
    private Boolean isRead;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}