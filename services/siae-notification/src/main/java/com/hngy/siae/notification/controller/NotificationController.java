package com.hngy.siae.notification.controller;

import cn.hutool.core.util.StrUtil;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.notification.dto.request.NotificationCreateDTO;
import com.hngy.siae.core.utils.JwtUtils;
import com.hngy.siae.notification.dto.request.NotificationBroadcastDTO;
import com.hngy.siae.notification.dto.response.NotificationVO;
import com.hngy.siae.notification.push.SsePushServiceImpl;
import com.hngy.siae.notification.service.NotificationService;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * 通知控制器
 *
 * @author KEYKB
 */
@Slf4j
@Tag(name = "通知管理", description = "系统通知相关接口")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SsePushServiceImpl ssePushService;
    private final JwtUtils jwtUtils;

    /**
     * SSE 实时通知流
     * 客户端通过此端点建立长连接，接收实时推送的通知
     * 支持两种认证方式：
     * 1. 通过 Spring Security Authentication（网关转发）
     * 2. 通过 query parameter 传递 token（EventSource 不支持自定义 headers）
     */
    @Operation(summary = "SSE通知流", description = "建立SSE连接接收实时通知推送")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> stream(
            Authentication authentication,
            @Parameter(description = "JWT Token（用于SSE认证）") @RequestParam(required = false) String token) {
        
        Long userId = null;
        
        // 优先从 Authentication 获取用户ID（网关转发的请求）
        if (authentication != null && authentication.getDetails() != null) {
            userId = (Long) authentication.getDetails();
            log.info("SSE stream auth from Authentication: userId={}", userId);
        }
        // 其次从 token query parameter 获取（EventSource 直连）
        else if (StrUtil.isNotBlank(token)) {
            try {
                if (jwtUtils.validateToken(token)) {
                    userId = jwtUtils.getUserIdFromToken(token);
                    log.info("SSE stream auth from token param: userId={}", userId);
                } else {
                    log.warn("SSE stream request with invalid token");
                }
            } catch (Exception e) {
                log.warn("SSE stream token validation failed: {}", e.getMessage());
            }
        }
        
        // 认证失败
        if (userId == null) {
            log.warn("SSE stream request without valid authentication");
            SseEmitter emitter = new SseEmitter(0L);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"code\":401,\"message\":\"未授权，请先登录\"}"));
                emitter.complete();
            } catch (IOException e) {
                log.error("Failed to send error event", e);
            }
            return ResponseEntity.status(401).body(emitter);
        }

        log.info("SSE stream opened for user: {}", userId);
        SseEmitter emitter = ssePushService.open(userId);
        return ResponseEntity.ok(emitter);
    }

    @Operation(summary = "发送通知", description = "发送系统通知（管理员）")
    @PostMapping("/send")
    @SiaeAuthorize
    public Result<Long> sendNotification(@Valid @RequestBody NotificationCreateDTO dto) {
        Long notificationId = notificationService.sendNotification(dto);
        return Result.success(notificationId);
    }

    @Operation(summary = "广播通知", description = "向所有用户或指定用户广播通知（管理员）")
    @PostMapping("/broadcast")
    @SiaeAuthorize
    public Result<Integer> broadcastNotification(@Valid @RequestBody NotificationBroadcastDTO dto) {
        int count = notificationService.broadcastNotification(dto);
        return Result.success(count);
    }

    @Operation(summary = "获取我的通知列表", description = "分页获取当前用户的通知列表")
    @GetMapping("/my")
    public Result<PageVO<NotificationVO>> getMyNotifications(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "是否已读") @RequestParam(required = false) Boolean isRead,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        PageVO<NotificationVO> result = notificationService.getUserNotifications(userId, page, size, isRead);
        return Result.success(result);
    }

    @Operation(summary = "获取未读通知数量", description = "获取当前用户未读通知数量")
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        Long count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    @Operation(summary = "标记通知状态", description = "标记指定通知为已读或未读")
    @PutMapping("/{id}/status")
    public Result<Void> markNotificationStatus(
            @Parameter(description = "通知ID") @PathVariable Long id,
            @Parameter(description = "是否已读") @RequestParam Boolean isRead,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        notificationService.updateReadStatus(id, userId, isRead);
        return Result.success();
    }

    @Operation(summary = "标记所有通知状态", description = "标记当前用户所有通知为已读或未读")
    @PutMapping("/status-all")
    public Result<Void> markAllNotificationStatus(
            @Parameter(description = "是否已读") @RequestParam Boolean isRead,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        notificationService.updateAllReadStatus(userId, isRead);
        return Result.success();
    }

    @Operation(summary = "删除通知", description = "删除指定通知")
    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(
            @Parameter(description = "通知ID") @PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        notificationService.deleteNotification(id, userId);
        return Result.success();
    }

    @Operation(summary = "批量删除已读通知", description = "删除当前用户的全部已读通知")
    @DeleteMapping("/read")
    public Result<Integer> deleteReadNotifications(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        int deletedCount = notificationService.deleteReadNotifications(userId);
        return Result.success(deletedCount);
    }

    /**
     * 从 Authentication 中获取用户ID
     */
    private Long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || authentication.getDetails() == null) {
            throw new IllegalStateException("未授权，请先登录");
        }
        return (Long) authentication.getDetails();
    }
}
