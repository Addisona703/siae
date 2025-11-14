package com.hngy.siae.messaging.autoconfig;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class RabbitTopologyUtils {

    private RabbitTopologyUtils() {
    }

    static Declarables buildDeclarables(Map<String, SiaeRabbitProperties.Exchange> exchangeConfigs,
                                        Map<String, SiaeRabbitProperties.Queue> queueConfigs,
                                        Map<String, SiaeRabbitProperties.BindingProperties> bindingConfigs) {
        List<Declarable> declarables = new ArrayList<>();

        if (exchangeConfigs != null) {
            exchangeConfigs.forEach((name, config) -> declarables.add(buildExchange(name, config)));
        }

        if (queueConfigs != null) {
            queueConfigs.forEach((name, config) -> declarables.add(buildQueue(name, config)));
        }

        if (bindingConfigs != null) {
            bindingConfigs.forEach((name, config) -> declarables.add(buildBinding(config)));
        }

        return new Declarables(declarables);
    }

    static void declareTopology(RabbitAdmin admin, Declarables declarables) {
        if (admin == null || declarables == null || CollectionUtils.isEmpty(declarables.getDeclarables())) {
            return;
        }
        // Declare in correct order: exchanges first, then queues, then bindings
        try {
            declarables.getDeclarables().stream()
                    .filter(d -> d instanceof Exchange)
                    .forEach(d -> {
                        Exchange exchange = (Exchange) d;
                        admin.declareExchange(exchange);
                    });
            
            declarables.getDeclarables().stream()
                    .filter(d -> d instanceof Queue)
                    .forEach(d -> {
                        Queue queue = (Queue) d;
                        admin.declareQueue(queue);
                    });
            
            declarables.getDeclarables().stream()
                    .filter(d -> d instanceof Binding)
                    .forEach(d -> {
                        Binding binding = (Binding) d;
                        admin.declareBinding(binding);
                    });
        } catch (Exception e) {
            throw new IllegalStateException("Failed to declare RabbitMQ topology", e);
        }
    }

    private static Exchange buildExchange(String name, SiaeRabbitProperties.Exchange config) {
        ExchangeBuilder builder = switch (config.getType().toLowerCase(Locale.ROOT)) {
            case "topic" -> ExchangeBuilder.topicExchange(name);
            case "direct" -> ExchangeBuilder.directExchange(name);
            case "fanout" -> ExchangeBuilder.fanoutExchange(name);
            case "headers" -> ExchangeBuilder.headersExchange(name);
            default -> throw new IllegalArgumentException("Unsupported exchange type: " + config.getType());
        };

        builder.durable(config.isDurable());
        if (config.isAutoDelete()) {
            builder.autoDelete();
        }
        if (config.isInternal()) {
            builder.internal();
        }
        if (config.isDelayed()) {
            builder.delayed();
        }
        if (config.getArguments() != null && !config.getArguments().isEmpty()) {
            builder.withArguments(config.getArguments());
        }
        return builder.build();
    }

    private static Queue buildQueue(String name, SiaeRabbitProperties.Queue config) {
        QueueBuilder builder = config.isDurable()
                ? QueueBuilder.durable(name)
                : QueueBuilder.nonDurable(name);
        if (config.isExclusive()) {
            builder.exclusive();
        }
        if (config.isAutoDelete()) {
            builder.autoDelete();
        }
        if (config.getArguments() != null && !config.getArguments().isEmpty()) {
            builder.withArguments(config.getArguments());
        }
        return builder.build();
    }

    private static Binding buildBinding(SiaeRabbitProperties.BindingProperties config) {
        return new Binding(
                config.getDestination(),
                config.getDestinationType(),
                config.getExchange(),
                config.getRoutingKey(),
                config.getArguments()
        );
    }
}
