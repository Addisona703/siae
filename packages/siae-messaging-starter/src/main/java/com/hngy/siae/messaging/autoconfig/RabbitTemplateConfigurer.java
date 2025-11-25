package com.hngy.siae.messaging.autoconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.util.StringUtils;

final class RabbitTemplateConfigurer {

    private static final Logger log = LoggerFactory.getLogger(RabbitTemplateConfigurer.class);

    private RabbitTemplateConfigurer() {
    }

    static void configureTemplate(RabbitTemplate template,
                                  SiaeRabbitProperties.Publisher publisher,
                                  String instanceName) {
        if (template == null || publisher == null) {
            return;
        }

        template.setMandatory(publisher.isReturns());

        if (publisher.isConfirms()) {
            template.setConfirmCallback((CorrelationData correlationData, boolean ack, String cause) -> {
                String messageId = correlationData != null ? correlationData.getId() : "";
                if (ack) {
                    if (log.isDebugEnabled()) {
                        log.debug("[SIAE-MQ] Confirm success (instance={}, messageId={})", instanceName, messageId);
                    }
                } else {
                    log.warn("[SIAE-MQ] Confirm failed (instance={}, messageId={}, cause={})",
                            instanceName, messageId, StringUtils.hasText(cause) ? cause : "unknown");
                }
            });
        } else {
            template.setConfirmCallback(null);
        }

        if (publisher.isReturns()) {
            template.setReturnsCallback((ReturnedMessage returned) -> {
                String messageId = returned.getMessage() != null
                        ? returned.getMessage().getMessageProperties().getMessageId()
                        : "";
                log.warn("[SIAE-MQ] Returned message (instance={}, replyCode={}, replyText={}, exchange={}, routingKey={}, messageId={})",
                        instanceName,
                        returned.getReplyCode(),
                        returned.getReplyText(),
                        returned.getExchange(),
                        returned.getRoutingKey(),
                        messageId);
            });
        } else {
            template.setReturnsCallback(null);
        }
    }
}
