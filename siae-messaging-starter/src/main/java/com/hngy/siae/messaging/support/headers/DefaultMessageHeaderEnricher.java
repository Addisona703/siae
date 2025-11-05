package com.hngy.siae.messaging.support.headers;

import com.hngy.siae.messaging.producer.MessageSendContext;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * 默认的消息头补全策略：填充 traceId、sentAt、tenantId 等通用字段。
 */
public class DefaultMessageHeaderEnricher implements MessageHeaderEnricher {

    public static final String HEADER_TRACE_ID = "traceId";
    public static final String HEADER_TENANT_ID = "tenantId";
    public static final String HEADER_SENT_AT = "sentAt";
    public static final String HEADER_MESSAGE_ID = "messageId";

    private static final String MDC_TRACE_ID = "traceId";
    private static final String MDC_TENANT_ID = "tenantId";

    @Override
    public void enrich(MessageSendContext context) {
        Map<String, Object> headers = context.getHeaders();

        headers.putIfAbsent(HEADER_MESSAGE_ID, context.getMessageId());
        headers.putIfAbsent(HEADER_SENT_AT, Instant.now().toString());

        String traceId = extractTraceId(headers, context);
        headers.put(HEADER_TRACE_ID, traceId);

        String tenantId = extractTenantId(headers);
        if (StringUtils.hasText(tenantId)) {
            headers.put(HEADER_TENANT_ID, tenantId);
        }
    }

    private String extractTraceId(Map<String, Object> headers, MessageSendContext context) {
        Object existing = headers.get(HEADER_TRACE_ID);
        if (existing instanceof String && StringUtils.hasText((String) existing)) {
            return (String) existing;
        }
        String mdcTraceId = MDC.get(MDC_TRACE_ID);
        if (!StringUtils.hasText(mdcTraceId)) {
            mdcTraceId = context.getMessageId();
        }
        if (!StringUtils.hasText(mdcTraceId)) {
            mdcTraceId = UUID.randomUUID().toString();
        }
        return mdcTraceId;
    }

    private String extractTenantId(Map<String, Object> headers) {
        Object header = headers.get(HEADER_TENANT_ID);
        if (header instanceof String && StringUtils.hasText((String) header)) {
            return (String) header;
        }
        return MDC.get(MDC_TENANT_ID);
    }
}
