package com.hngy.siae.media.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.MediaResultCodeEnum;
import com.hngy.siae.media.domain.event.FileEvent;
import com.hngy.siae.messaging.producer.SiaeMessagingTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 事件发布器
 * 使用 siae-messaging-starter 的 SiaeMessagingTemplate 发送消息
 *
 * @author SIAE Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final SiaeMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    private static final String EXCHANGE_FILE_EVENTS = "media.file.events";

    /**
     * 发布文件事件
     */
    public void publishFileEvent(FileEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            String routingKey = event.getEventType(); // 使用事件类型作为路由键

            // 使用 SiaeMessagingTemplate 发送消息
            messagingTemplate.send(EXCHANGE_FILE_EVENTS, routingKey, message);

            log.info("Published event: type={}, fileId={}, exchange={}, routingKey={}", 
                    event.getEventType(), event.getFileId(), EXCHANGE_FILE_EVENTS, routingKey);

        } catch (Exception e) {
            log.error("Failed to publish event: type={}, fileId={}", 
                    event.getEventType(), event.getFileId(), e);
            AssertUtils.fail(MediaResultCodeEnum.EVENT_PUBLISH_FAILED);
        }
    }

}
