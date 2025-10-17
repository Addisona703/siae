package com.hngy.siae.notification.controller;

import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.notification.dto.request.NotificationCreateDTO;
import com.hngy.siae.notification.dto.response.NotificationVO;
import com.hngy.siae.notification.service.NotificationService;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
            Authentication authentication) {

        Long userId = (Long) authentication.getDetails();
        PageVO<NotificationVO> result = notificationService.getUserNotifications(userId, page, size, isRead);
        return Result.success(result);
    }

    @Operation(summary = "获取未读通知数量", description = "获取当前用户未读通知数量")
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        Long count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    @Operation(summary = "标记通知为已读", description = "标记指定通知为已读")
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(
            @Parameter(description = "通知ID") @PathVariable Long id,
            Authentication authentication) {

        Long userId = (Long) authentication.getDetails();
        notificationService.markAsRead(id, userId);
        return Result.success();
    }

    @Operation(summary = "标记所有通知为已读", description = "标记当前用户所有未读通知为已读")
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        notificationService.markAllAsRead(userId);
        return Result.success();
    }

    @Operation(summary = "删除通知", description = "删除指定通知")
    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(
            @Parameter(description = "通知ID") @PathVariable Long id,
            Authentication authentication) {

        Long userId = (Long) authentication.getDetails();
        notificationService.deleteNotification(id, userId);
        return Result.success();
    }
}