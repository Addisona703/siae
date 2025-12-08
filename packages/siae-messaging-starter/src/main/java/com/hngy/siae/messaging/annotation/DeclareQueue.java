package com.hngy.siae.messaging.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明队列注解，用于自动创建 RabbitMQ 队列、交换机和绑定关系。
 * <p>
 * 可以标注在类或方法上，支持重复注解。
 * </p>
 *
 * <pre>
 * // 示例1：简单队列声明
 * {@literal @}DeclareQueue(queue = "my.queue")
 *
 * // 示例2：完整声明（队列 + 交换机 + 绑定）
 * {@literal @}DeclareQueue(
 *     queue = "order.queue",
 *     exchange = "order.exchange",
 *     routingKey = "order.#",
 *     exchangeType = ExchangeType.TOPIC
 * )
 *
 * // 示例3：使用常量
 * {@literal @}DeclareQueue(
 *     queue = MessagingConstants.NOTIFICATION_QUEUE,
 *     exchange = MessagingConstants.NOTIFICATION_EXCHANGE,
 *     routingKey = "notification.#"
 * )
 * </pre>
 *
 * @author KEYKB
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(DeclareQueues.class)
public @interface DeclareQueue {

    /**
     * 队列名称（必填）
     */
    String queue();

    /**
     * 交换机名称（可选，不填则只创建队列）
     */
    String exchange() default "";

    /**
     * 路由键（可选，默认为队列名）
     */
    String routingKey() default "";

    /**
     * 交换机类型
     */
    ExchangeType exchangeType() default ExchangeType.TOPIC;

    /**
     * 队列是否持久化
     */
    boolean durable() default true;

    /**
     * 队列是否自动删除
     */
    boolean autoDelete() default false;

    /**
     * 队列是否排他
     */
    boolean exclusive() default false;

    /**
     * 交换机是否持久化
     */
    boolean exchangeDurable() default true;

    /**
     * 交换机是否自动删除
     */
    boolean exchangeAutoDelete() default false;

    /**
     * 死信交换机（可选）
     */
    String deadLetterExchange() default "";

    /**
     * 死信路由键（可选）
     */
    String deadLetterRoutingKey() default "";

    /**
     * 消息 TTL（毫秒，0 表示不设置）
     */
    long messageTtl() default 0;

    /**
     * 队列最大长度（0 表示不限制）
     */
    int maxLength() default 0;
}
