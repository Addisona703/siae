package com.hngy.siae.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 通知消息实体（用于跨服务消息传递）
 * 
 * @author KEYKB
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 通知类型：1=系统通知,2=公告,3=提醒
     */
    private Integer type;

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
     * 业务ID（可选，用于关联业务对象）
     */
    private Long businessId;

    /**
     * 业务类型（可选，如：ORDER、CONTENT、COMMENT等）
     */
    private String businessType;
}
