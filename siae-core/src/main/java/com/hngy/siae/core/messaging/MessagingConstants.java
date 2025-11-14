package com.hngy.siae.core.messaging;

/**
 * 消息队列常量定义
 * 
 * @author KEYKB
 */
public class MessagingConstants {

    // ==================== 交换机 ====================
    
    /**
     * 通知交换机
     */
    public static final String NOTIFICATION_EXCHANGE = "siae.notification.exchange";

    // ==================== 队列 ====================
    
    /**
     * 站内通知队列
     */
    public static final String NOTIFICATION_QUEUE = "siae.notification.queue";

    /**
     * 邮件通知队列
     */
    public static final String EMAIL_QUEUE = "siae.email.queue";

    /**
     * 短信通知队列
     */
    public static final String SMS_QUEUE = "siae.sms.queue";

    // ==================== 路由键 ====================
    
    /**
     * 站内通知路由键前缀
     */
    public static final String NOTIFICATION_ROUTING_PREFIX = "notification.";

    /**
     * 邮件通知路由键前缀
     */
    public static final String EMAIL_ROUTING_PREFIX = "email.";

    /**
     * 短信通知路由键前缀
     */
    public static final String SMS_ROUTING_PREFIX = "sms.";

    // ==================== 具体路由键 ====================
    
    // 站内通知
    public static final String NOTIFICATION_SYSTEM = "notification.system";
    public static final String NOTIFICATION_ORDER = "notification.order";
    public static final String NOTIFICATION_CONTENT = "notification.content";
    public static final String NOTIFICATION_COMMENT = "notification.comment";

    // 邮件通知
    public static final String EMAIL_VERIFICATION = "email.verification";
    public static final String EMAIL_NOTIFICATION = "email.notification";

    // 短信通知
    public static final String SMS_VERIFICATION = "sms.verification";
    public static final String SMS_NOTIFICATION = "sms.notification";

    private MessagingConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
