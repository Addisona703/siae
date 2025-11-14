package com.hngy.siae.notification.dto.response;

import com.hngy.siae.notification.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知响应VO
 *
 * @author KEYKB
 */
@Data
@Schema(description = "通知信息")
public class NotificationVO {

    @Schema(description = "通知ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "通知类型")
    private NotificationType type;

    @Schema(description = "通知标题")
    private String title;

    @Schema(description = "通知内容")
    private String content;

    @Schema(description = "跳转链接")
    private String linkUrl;

    @Schema(description = "是否已读")
    private Boolean isRead;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}