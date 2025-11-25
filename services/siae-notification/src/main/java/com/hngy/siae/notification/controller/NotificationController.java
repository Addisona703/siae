package com.hngy.siae.notification.controller;

import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.notification.dto.request.NotificationCreateDTO;
import com.hngy.siae.notification.dto.response.NotificationVO;
import com.hngy.siae.notification.push.SsePushServiceImpl;
import com.hngy.siae.notification.service.NotificationService;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 通知控制器
 *
 * @author KEYKB
 */
@Tag(name = "通知管理", description = "系统通知相关接口")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SsePushServiceImpl ssePushService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        return ssePushService.open(userId);
    }

    @Operation(summary = "发送通知", description = "发送系统通知（管理员）")
    @PostMapping("/send")
    @SiaeAuthorize
    public Result<Long> sendNotification(@Valid @RequestBody NotificationCreateDTO dto) {
        Long notificationId = notificationService.sendNotification(dto);
        return Result.success(notificationId);
    }

    @Operation(summary = "获取我的通知列表", description = "分页获取当前用户的通知列表")
    @GetMapping("/my")
    public Result<PageVO<NotificationVO>> getMyNotifications(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "是否已读") @RequestParam(required = false) Boolean isRead,
            @Parameter(description = "用户ID(测试用)") @RequestParam(required = false) Long userId,
            Authentication authentication) {

        // 优先从 authentication 获取,如果没有则使用传入的 userId(用于测试)
        Long actualUserId = authentication != null ? (Long) authentication.getDetails() : userId;
        if (actualUserId == null) {
            actualUserId = 1L; // 默认使用用户ID 1 进行测试
        }
        PageVO<NotificationVO> result = notificationService.getUserNotifications(actualUserId, page, size, isRead);
        return Result.success(result);
    }

    @Operation(summary = "获取未读通知数量", description = "获取当前用户未读通知数量")
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(
            @Parameter(description = "用户ID(测试用)") @RequestParam(required = false) Long userId,
            Authentication authentication) {
        
        // 优先从 authentication 获取,如果没有则使用传入的 userId(用于测试)
        Long actualUserId = authentication != null ? (Long) authentication.getDetails() : userId;
        if (actualUserId == null) {
            actualUserId = 1L; // 默认使用用户ID 1 进行测试
        }
        Long count = notificationService.getUnreadCount(actualUserId);
        return Result.success(count);
    }

    @Operation(summary = "标记通知状态", description = "标记指定通知为已读或未读")
    @PutMapping("/{id}/status")
    public Result<Void> markNotificationStatus(
            @Parameter(description = "通知ID") @PathVariable Long id,
            @Parameter(description = "是否已读") @RequestParam Boolean isRead,
            @Parameter(description = "用户ID(测试用)") @RequestParam(required = false) Long userId,
            Authentication authentication) {

        // 优先从 authentication 获取,如果没有则使用传入的 userId(用于测试)
        Long actualUserId = authentication != null ? (Long) authentication.getDetails() : userId;
        if (actualUserId == null) {
            actualUserId = 1L; // 默认使用用户ID 1 进行测试
        }
        notificationService.updateReadStatus(id, actualUserId, isRead);
        return Result.success();
    }

    @Operation(summary = "标记所有通知状态", description = "标记当前用户所有通知为已读或未读")
    @PutMapping("/status-all")
    public Result<Void> markAllNotificationStatus(
            @Parameter(description = "是否已读") @RequestParam Boolean isRead,
            @Parameter(description = "用户ID(测试用)") @RequestParam(required = false) Long userId,
            Authentication authentication) {
        
        // 优先从 authentication 获取,如果没有则使用传入的 userId(用于测试)
        Long actualUserId = authentication != null ? (Long) authentication.getDetails() : userId;
        if (actualUserId == null) {
            actualUserId = 1L; // 默认使用用户ID 1 进行测试
        }
        notificationService.updateAllReadStatus(actualUserId, isRead);
        return Result.success();
    }

    @Operation(summary = "删除通知", description = "删除指定通知")
    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(
            @Parameter(description = "通知ID") @PathVariable Long id,
            @Parameter(description = "用户ID(测试用)") @RequestParam(required = false) Long userId,
            Authentication authentication) {

        // 优先从 authentication 获取,如果没有则使用传入的 userId(用于测试)
        Long actualUserId = authentication != null ? (Long) authentication.getDetails() : userId;
        if (actualUserId == null) {
            actualUserId = 1L; // 默认使用用户ID 1 进行测试
        }
        notificationService.deleteNotification(id, actualUserId);
        return Result.success();
    }
}
