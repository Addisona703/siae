package com.hngy.siae.messaging.annotation;

/**
 * 交换机类型枚举
 *
 * @author KEYKB
 */
public enum ExchangeType {

    /**
     * 直连交换机
     */
    DIRECT,

    /**
     * 主题交换机
     */
    TOPIC,

    /**
     * 扇出交换机（广播）
     */
    FANOUT,

    /**
     * 头部交换机
     */
    HEADERS
}
