package com.hngy.siae.notification.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.messaging.event.SmsMessage;
import com.hngy.siae.notification.entity.SmsLog;

/**
 * 短信日志服务接口
 *
 * @author KEYKB
 */
public interface SmsLogService extends IService<SmsLog> {

    /**
     * 处理短信消息（发送短信并记录日志）
     *
     * @param message 短信消息
     */
    void handleSmsMessage(SmsMessage message);
}
