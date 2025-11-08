package com.hngy.siae.messaging.consumer;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RabbitListener(containerFactory = "siaeListenerContainerFactory", errorHandler = "siaeRabbitListenerErrorHandler")
public @interface SiaeRabbitListener {

    @AliasFor(annotation = RabbitListener.class, attribute = "id")
    String id() default "";

    @AliasFor(annotation = RabbitListener.class, attribute = "group")
    String group() default "";

    @AliasFor(annotation = RabbitListener.class, attribute = "queues")
    String[] queues() default {};

    @AliasFor(annotation = RabbitListener.class, attribute = "bindings")
    QueueBinding[] bindings() default {};

    @AliasFor(annotation = RabbitListener.class, attribute = "concurrency")
    String concurrency() default "";

    @AliasFor(annotation = RabbitListener.class, attribute = "ackMode")
    String ackMode() default "";

    @AliasFor(annotation = RabbitListener.class, attribute = "autoStartup")
    String autoStartup() default "";

    @AliasFor(annotation = RabbitListener.class, attribute = "exclusive")
    boolean exclusive() default false;

    @AliasFor(annotation = RabbitListener.class, attribute = "admin")
    String admin() default "";

    @AliasFor(annotation = RabbitListener.class, attribute = "returnExceptions")
    String returnExceptions() default "";

    /**
     * 快捷创建 Queue.
     */
    @AliasFor(annotation = RabbitListener.class, attribute = "queuesToDeclare")
    Queue[] queuesToDeclare() default {};
}
