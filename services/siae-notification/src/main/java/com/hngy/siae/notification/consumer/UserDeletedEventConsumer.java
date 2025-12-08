package com.hngy.siae.notification.consumer;

import com.hngy.siae.messaging.annotation.DeclareQueue;
import com.hngy.siae.messaging.annotation.ExchangeType;
import com.hngy.siae.messaging.event.MessagingConstants;
import com.hngy.siae.messaging.event.UserDeletedEvent;
import com.hngy.siae.messaging.consumer.SiaeRabbitListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户删除事件消费者
 * <p>
 * 监听用户服务发送的用户删除事件，清理通知服务中的关联数据
 *
 * @author KEYKB
 */
@Slf4j
@Component
@RequiredArgsConstructor
@DeclareQueue(
        queue = MessagingConstants.CLEANUP_USER_NOTIFICATION_QUEUE,
        exchange = MessagingConstants.CLEANUP_EXCHANGE,
        routingKey = MessagingConstants.CLEANUP_USER_DELETED,
        exchangeType = ExchangeType.TOPIC
)
public class UserDeletedEventConsumer {

    // TODO: 注入需要的 Service 或 Mapper
    // private final NotificationMapper notificationMapper;

    /**
     * 处理用户删除事件
     * 清理该用户在通知服务中的相关数据
     */
    @SiaeRabbitListener(queues = MessagingConstants.CLEANUP_USER_NOTIFICATION_QUEUE)
    public void handleUserDeletedEvent(UserDeletedEvent event) {
        log.info("收到用户删除事件: eventId={}, userIds={}", event.getEventId(), event.getUserIds());

        List<Long> userIds = event.getUserIds();
        if (userIds == null || userIds.isEmpty()) {
            log.warn("用户ID列表为空，跳过处理");
            return;
        }

        try {
            // 1. 清理用户的通知记录
            // int notificationCount = notificationMapper.deleteByUserIds(userIds);
            // log.info("删除用户通知: {} 条", notificationCount);

            // 2. 清理用户的邮件发送记录
            // int emailLogCount = emailLogMapper.deleteByUserIds(userIds);
            // log.info("删除邮件记录: {} 条", emailLogCount);

            log.info("用户删除事件处理完成: eventId={}", event.getEventId());

        } catch (Exception e) {
            log.error("处理用户删除事件失败: eventId={}, userIds={}", event.getEventId(), userIds, e);
            throw new RuntimeException("处理用户删除事件失败", e);
        }
    }
}
