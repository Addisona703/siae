package com.hngy.siae.notification.dto.request;

import com.hngy.siae.notification.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建通知请求DTO
 *
 * @author KEYKB
 */
@Data
@Schema(description = "创建通知请求")
public class NotificationCreateDTO {

    @Schema(description = "用户ID")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "通知类型")
    @NotNull(message = "通知类型不能为空")
    private NotificationType type;

    @Schema(description = "通知标题")
    @NotBlank(message = "通知标题不能为空")
    private String title;

    @Schema(description = "通知内容")
    private String content;

    @Schema(description = "跳转链接")
    private String linkUrl;
}