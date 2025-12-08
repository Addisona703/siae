package com.hngy.siae.notification.events;

import com.hngy.siae.notification.push.RealtimePush;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.time.LocalDateTime;

/**
 * 事件监听器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final RealtimePush push;

    // 当事务提交后才推送消息
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCreated(NotificationCreatedEvent evt) {
        var n = evt.notification();
        Payload payload = new Payload(
                n.getId(),
                n.getUserId(),
                n.getType() != null ? n.getType().getCode() : null,
                n.getTitle(),
                n.getContent(),
                n.getLinkUrl(),
                n.getIsRead() != null ? n.getIsRead() : false,
                n.getCreatedAt() != null ? n.getCreatedAt() : LocalDateTime.now()
        );
        push.pushToUser(n.getUserId(), payload);
        log.info("SSE 推送完成 - userId={}, notificationId={}", n.getUserId(), n.getId());
    }

    /** 完整传输体，与前端 normalizeNotification 字段对应 */
    record Payload(
            Long id,
            Long userId,
            Integer type,
            String title,
            String content,
            String linkUrl,
            Boolean isRead,
            LocalDateTime createdAt
    ) {}
}
