package com.hngy.siae.notification.consumer;

import com.hngy.siae.messaging.annotation.DeclareQueue;
import com.hngy.siae.messaging.annotation.ExchangeType;
import com.hngy.siae.messaging.event.MessagingConstants;
import com.hngy.siae.messaging.event.SmsMessage;
import com.hngy.siae.messaging.consumer.SiaeRabbitListener;
import com.hngy.siae.notification.service.SmsLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 短信消息消费者
 *
 * @author KEYKB
 */
@Slf4j
@Component
@RequiredArgsConstructor
@DeclareQueue(
        queue = MessagingConstants.SMS_QUEUE,
        exchange = MessagingConstants.NOTIFICATION_EXCHANGE,
        routingKey = "sms.#",
        exchangeType = ExchangeType.TOPIC
)
public class SmsConsumer {

    private final SmsLogService smsLogService;

    /**
     * 消费短信消息
     */
    @SiaeRabbitListener(queues = MessagingConstants.SMS_QUEUE)
    public void handleSms(SmsMessage message) {
        log.info("收到短信消息: phone={}, content={}", 
                message.getPhone(), message.getContent());
        
        // 委托给 Service 处理
        smsLogService.handleSmsMessage(message);
    }
}
