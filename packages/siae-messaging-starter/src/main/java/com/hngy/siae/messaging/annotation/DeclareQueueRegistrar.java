package com.hngy.siae.messaging.annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 扫描 {@link DeclareQueue} 注解并自动声明队列、交换机和绑定。
 *
 * @author KEYKB
 */
public class DeclareQueueRegistrar implements BeanPostProcessor, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(DeclareQueueRegistrar.class);

    private ApplicationContext applicationContext;
    private RabbitAdmin rabbitAdmin;

    /**
     * 已声明的队列名称（避免重复声明）
     */
    private final Set<String> declaredQueues = ConcurrentHashMap.newKeySet();

    /**
     * 已声明的交换机名称
     */
    private final Set<String> declaredExchanges = ConcurrentHashMap.newKeySet();

    /**
     * 已声明的绑定（exchange:queue:routingKey）
     */
    private final Set<String> declaredBindings = ConcurrentHashMap.newKeySet();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();

        // 扫描类上的注解
        processClassAnnotations(targetClass);

        // 扫描方法上的注解
        for (Method method : targetClass.getDeclaredMethods()) {
            processMethodAnnotations(method);
        }

        return bean;
    }

    private void processClassAnnotations(Class<?> targetClass) {
        // 处理单个 @DeclareQueue
        DeclareQueue declareQueue = AnnotationUtils.findAnnotation(targetClass, DeclareQueue.class);
        if (declareQueue != null) {
            processAnnotation(declareQueue);
        }

        // 处理 @DeclareQueues（多个）
        DeclareQueues declareQueues = AnnotationUtils.findAnnotation(targetClass, DeclareQueues.class);
        if (declareQueues != null) {
            for (DeclareQueue annotation : declareQueues.value()) {
                processAnnotation(annotation);
            }
        }
    }

    private void processMethodAnnotations(Method method) {
        // 处理单个 @DeclareQueue
        DeclareQueue declareQueue = AnnotationUtils.findAnnotation(method, DeclareQueue.class);
        if (declareQueue != null) {
            processAnnotation(declareQueue);
        }

        // 处理 @DeclareQueues（多个）
        DeclareQueues declareQueues = AnnotationUtils.findAnnotation(method, DeclareQueues.class);
        if (declareQueues != null) {
            for (DeclareQueue annotation : declareQueues.value()) {
                processAnnotation(annotation);
            }
        }
    }

    private void processAnnotation(DeclareQueue annotation) {
        String queueName = annotation.queue();
        if (!StringUtils.hasText(queueName)) {
            return;
        }

        RabbitAdmin admin = getRabbitAdmin();
        if (admin == null) {
            log.warn("[SIAE-MQ] RabbitAdmin not available, skipping queue declaration: {}", queueName);
            return;
        }

        try {
            // 1. 声明队列
            declareQueue(admin, annotation);

            // 2. 声明交换机（如果指定）
            String exchangeName = annotation.exchange();
            if (StringUtils.hasText(exchangeName)) {
                declareExchange(admin, annotation);

                // 3. 声明绑定
                declareBinding(admin, annotation);
            }
        } catch (Exception e) {
            log.error("[SIAE-MQ] Failed to declare queue topology for: {}", queueName, e);
        }
    }

    private void declareQueue(RabbitAdmin admin, DeclareQueue annotation) {
        String queueName = annotation.queue();
        if (declaredQueues.contains(queueName)) {
            return;
        }

        QueueBuilder builder = annotation.durable()
                ? QueueBuilder.durable(queueName)
                : QueueBuilder.nonDurable(queueName);

        if (annotation.exclusive()) {
            builder.exclusive();
        }
        if (annotation.autoDelete()) {
            builder.autoDelete();
        }

        // 死信配置
        if (StringUtils.hasText(annotation.deadLetterExchange())) {
            builder.deadLetterExchange(annotation.deadLetterExchange());
            if (StringUtils.hasText(annotation.deadLetterRoutingKey())) {
                builder.deadLetterRoutingKey(annotation.deadLetterRoutingKey());
            }
        }

        // TTL 配置
        if (annotation.messageTtl() > 0) {
            builder.ttl((int) annotation.messageTtl());
        }

        // 最大长度
        if (annotation.maxLength() > 0) {
            builder.maxLength(annotation.maxLength());
        }

        Queue queue = builder.build();
        admin.declareQueue(queue);
        declaredQueues.add(queueName);
        log.info("[SIAE-MQ] Auto-declared queue: {}", queueName);
    }

    private void declareExchange(RabbitAdmin admin, DeclareQueue annotation) {
        String exchangeName = annotation.exchange();
        if (!StringUtils.hasText(exchangeName) || declaredExchanges.contains(exchangeName)) {
            return;
        }

        Exchange exchange = createExchange(exchangeName, annotation);
        admin.declareExchange(exchange);
        declaredExchanges.add(exchangeName);
        log.info("[SIAE-MQ] Auto-declared exchange: {} (type={})", exchangeName, annotation.exchangeType());
    }

    private Exchange createExchange(String name, DeclareQueue annotation) {
        boolean durable = annotation.exchangeDurable();
        boolean autoDelete = annotation.exchangeAutoDelete();

        return switch (annotation.exchangeType()) {
            case DIRECT -> new DirectExchange(name, durable, autoDelete);
            case FANOUT -> new FanoutExchange(name, durable, autoDelete);
            case HEADERS -> new HeadersExchange(name, durable, autoDelete);
            case TOPIC -> new TopicExchange(name, durable, autoDelete);
        };
    }

    private void declareBinding(RabbitAdmin admin, DeclareQueue annotation) {
        String exchangeName = annotation.exchange();
        String queueName = annotation.queue();
        String routingKey = StringUtils.hasText(annotation.routingKey())
                ? annotation.routingKey()
                : queueName;

        String bindingKey = exchangeName + ":" + queueName + ":" + routingKey;
        if (declaredBindings.contains(bindingKey)) {
            return;
        }

        Binding binding = new Binding(
                queueName,
                Binding.DestinationType.QUEUE,
                exchangeName,
                routingKey,
                null
        );

        admin.declareBinding(binding);
        declaredBindings.add(bindingKey);
        log.info("[SIAE-MQ] Auto-declared binding: {} -> {} (routingKey={})",
                exchangeName, queueName, routingKey);
    }

    private RabbitAdmin getRabbitAdmin() {
        if (rabbitAdmin == null) {
            try {
                rabbitAdmin = applicationContext.getBean(RabbitAdmin.class);
            } catch (BeansException e) {
                log.debug("[SIAE-MQ] RabbitAdmin bean not found");
            }
        }
        return rabbitAdmin;
    }

    /**
     * 获取已声明的队列数量
     */
    public int getDeclaredQueueCount() {
        return declaredQueues.size();
    }

    /**
     * 获取已声明的交换机数量
     */
    public int getDeclaredExchangeCount() {
        return declaredExchanges.size();
    }

    /**
     * 获取已声明的绑定数量
     */
    public int getDeclaredBindingCount() {
        return declaredBindings.size();
    }
}
