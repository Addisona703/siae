package com.hngy.siae.notification.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.core.messaging.EmailMessage;
import com.hngy.siae.notification.entity.EmailLog;

/**
 * 邮件日志服务接口
 *
 * @author KEYKB
 */
public interface EmailLogService extends IService<EmailLog> {

    /**
     * 处理邮件消息（发送邮件并记录日志）
     *
     * @param message 邮件消息
     */
    void handleEmailMessage(EmailMessage message);
}
