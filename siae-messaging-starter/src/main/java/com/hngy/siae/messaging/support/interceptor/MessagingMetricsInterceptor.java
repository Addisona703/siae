package com.hngy.siae.messaging.support.interceptor;

import com.hngy.siae.messaging.producer.MessageSendContext;
import com.hngy.siae.messaging.producer.MessageSendInterceptor;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MessagingMetricsInterceptor implements MessageSendInterceptor {

    private static final String METRIC_SENT = "siae.messaging.producer.sent";
    private static final String METRIC_FAILED = "siae.messaging.producer.failed";
    private static final String METRIC_LATENCY = "siae.messaging.producer.latency";

    private final MeterRegistry meterRegistry;
    private final ThreadLocal<Timer.Sample> sampleThreadLocal = new ThreadLocal<>();
    private final ConcurrentMap<String, Counter> counterCache = new ConcurrentHashMap<>();

    public MessagingMetricsInterceptor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void beforeSend(MessageSendContext context) {
        if (meterRegistry == null) {
            return;
        }
        sampleThreadLocal.set(Timer.start(meterRegistry));
    }

    @Override
    public void afterSend(MessageSendContext context) {
        if (meterRegistry == null) {
            return;
        }
        stopTimer(context, METRIC_LATENCY);
        counter(context, METRIC_SENT).increment();
    }

    @Override
    public void onError(MessageSendContext context, Throwable throwable) {
        if (meterRegistry == null) {
            return;
        }
        stopTimer(context, METRIC_LATENCY);
        counter(context, METRIC_FAILED).increment();
    }

    private void stopTimer(MessageSendContext context, String metricName) {
        Timer.Sample sample = sampleThreadLocal.get();
        sampleThreadLocal.remove();
        if (sample == null) {
            return;
        }
        List<Tag> tags = tags(context);
        sample.stop(Timer.builder(metricName).tags(tags).register(meterRegistry));
    }

    private Counter counter(MessageSendContext context, String metricName) {
        return counterCache.computeIfAbsent(metricName + tagsKey(context),
                key -> Counter.builder(metricName)
                        .tags(tags(context))
                        .register(meterRegistry));
    }

    private List<Tag> tags(MessageSendContext context) {
        List<Tag> tags = new ArrayList<>(4);
        tags.add(Tag.of("exchange", safeTagValue(context.getExchange())));
        tags.add(Tag.of("routingKey", safeTagValue(context.getRoutingKey())));
        tags.add(Tag.of("attempt", Integer.toString(context.getAttempt())));
        tags.add(Tag.of("tenantId", safeTagValue(String.valueOf(context.getHeaders().getOrDefault("tenantId", "unknown")))));
        return tags;
    }

    private String tagsKey(MessageSendContext context) {
        return safeTagValue(context.getExchange()) + "|" +
                safeTagValue(context.getRoutingKey()) + "|" +
                context.getAttempt() + "|" +
                safeTagValue(String.valueOf(context.getHeaders().getOrDefault("tenantId", "unknown")));
    }

    private String safeTagValue(String value) {
        return StringUtils.hasText(value) ? value : "unknown";
    }
}
