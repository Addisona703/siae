package com.hngy.siae.notification.consumer;

import com.hngy.siae.messaging.annotation.DeclareQueue;
import com.hngy.siae.messaging.annotation.ExchangeType;
import com.hngy.siae.messaging.event.MessagingConstants;
import com.hngy.siae.messaging.event.NotificationMessage;
import com.hngy.siae.messaging.consumer.SiaeRabbitListener;
import com.hngy.siae.notification.config.NotificationProperties;
import com.hngy.siae.notification.dto.request.NotificationCreateDTO;
import com.hngy.siae.notification.enums.NotificationType;
import com.hngy.siae.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 站内通知消息消费者
 *
 * @author KEYKB
 */
@Slf4j
@Component
@RequiredArgsConstructor
@DeclareQueue(
        queue = MessagingConstants.NOTIFICATION_QUEUE,
        exchange = MessagingConstants.NOTIFICATION_EXCHANGE,
        routingKey = "notification.#",
        exchangeType = ExchangeType.TOPIC
)
public class NotificationConsumer {

    private final NotificationService notificationService;
    private final NotificationProperties notificationProperties;

    /**
     * 消费站内通知消息
     */
    @SiaeRabbitListener(queues = MessagingConstants.NOTIFICATION_QUEUE)
    public void handleNotification(NotificationMessage message) {
        log.info("收到站内通知消息: userId={}, type={}, title={}", 
                message.getUserId(), message.getType(), message.getTitle());

        try {
            // 转换为 DTO
            NotificationCreateDTO dto = new NotificationCreateDTO();
            dto.setUserId(message.getUserId());
            dto.setType(NotificationType.fromCode(message.getType()));
            dto.setTitle(message.getTitle());
            dto.setContent(message.getContent());
            
            // 拼接完整的前端URL（其他服务只需传相对路径如 /content/11）
            dto.setLinkUrl(notificationProperties.buildFullUrl(message.getLinkUrl()));

            // 发送通知（保存到数据库 + SSE推送）
            Long notificationId = notificationService.sendNotification(dto);

            log.info("站内通知发送成功: notificationId={}, userId={}, linkUrl={}", 
                    notificationId, message.getUserId(), dto.getLinkUrl());

        } catch (Exception e) {
            log.error("处理站内通知消息失败: userId={}, title={}", 
                    message.getUserId(), message.getTitle(), e);
            throw e; // 抛出异常触发重试
        }
    }
}
