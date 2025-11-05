package com.hngy.siae.notification.events;

import com.hngy.siae.notification.push.RealtimePush;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

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
        push.pushToUser(n.getUserId(), new Payload(n.getId(), n.getTitle(), n.getContent()));
        log.info("SSE 推送完成 - userId={}, notificationId={}", n.getUserId(), n.getId());
    }

    /** 轻量传输体，避免把实体直接抛给前端 */
    record Payload(Long id, String title, String content) {}
}
