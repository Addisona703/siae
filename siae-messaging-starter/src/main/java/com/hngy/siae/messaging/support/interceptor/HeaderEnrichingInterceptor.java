package com.hngy.siae.messaging.support.interceptor;

import com.hngy.siae.messaging.producer.MessageSendContext;
import com.hngy.siae.messaging.producer.MessageSendInterceptor;
import com.hngy.siae.messaging.support.headers.MessageHeaderEnricher;

public class HeaderEnrichingInterceptor implements MessageSendInterceptor {

    private final MessageHeaderEnricher enricher;

    public HeaderEnrichingInterceptor(MessageHeaderEnricher enricher) {
        this.enricher = enricher;
    }

    @Override
    public void beforeSend(MessageSendContext context) {
        enricher.enrich(context);
    }
}
