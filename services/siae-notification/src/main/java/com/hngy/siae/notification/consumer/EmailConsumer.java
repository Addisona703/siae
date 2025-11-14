package com.hngy.siae.notification.consumer;

import com.hngy.siae.core.messaging.EmailMessage;
import com.hngy.siae.core.messaging.MessagingConstants;
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
