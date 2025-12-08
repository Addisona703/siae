package com.hngy.siae.notification.consumer;

import com.hngy.siae.messaging.annotation.DeclareQueue;
import com.hngy.siae.messaging.annotation.ExchangeType;
import com.hngy.siae.messaging.event.EmailMessage;
import com.hngy.siae.messaging.event.MessagingConstants;
import com.hngy.siae.messaging.consumer.SiaeRabbitListener;
import com.hngy.siae.notification.service.EmailLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 邮件消息消费者
 *
 * @author KEYKB
 */
@Slf4j
@Component
@RequiredArgsConstructor
@DeclareQueue(
        queue = MessagingConstants.EMAIL_QUEUE,
        exchange = MessagingConstants.NOTIFICATION_EXCHANGE,
        routingKey = "email.#",
        exchangeType = ExchangeType.TOPIC
)
public class EmailConsumer {

    private final EmailLogService emailLogService;

    /**
     * 消费邮件消息
     */
    @SiaeRabbitListener(queues = MessagingConstants.EMAIL_QUEUE)
    public void handleEmail(EmailMessage message) {
        log.info("收到邮件消息: recipient={}, subject={}", 
                message.getRecipient(), message.getSubject());
        
        // 委托给 Service 处理
        emailLogService.handleEmailMessage(message);
    }
}
