package com.hngy.siae.notification.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.messaging.SmsMessage;
import com.hngy.siae.notification.entity.SmsLog;
import com.hngy.siae.notification.enums.SendStatus;
import com.hngy.siae.notification.mapper.SmsLogMapper;
import com.hngy.siae.notification.service.SmsLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 短信日志服务实现类
 *
 * @author KEYKB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsLogServiceImpl extends ServiceImpl<SmsLogMapper, SmsLog> implements SmsLogService {

    @Override
    public void handleSmsMessage(SmsMessage message) {
        log.info("开始处理短信消息: phone={}, content={}", 
                message.getPhone(), message.getContent());

        // 创建短信日志记录
        SmsLog smsLog = new SmsLog();
        smsLog.setPhone(message.getPhone());
        smsLog.setContent(message.getContent());
        smsLog.setTemplateCode(message.getTemplateCode());
        smsLog.setStatus(SendStatus.PENDING);

        try {
            // 发送短信
            sendSms(message);

            // 更新状态为成功
            smsLog.setStatus(SendStatus.SUCCESS);
            smsLog.setSendTime(LocalDateTime.now());
            log.info("短信发送成功: phone={}", message.getPhone());

        } catch (Exception e) {
            // 更新状态为失败
            smsLog.setStatus(SendStatus.FAILED);
            smsLog.setErrorMsg(e.getMessage());
            log.error("短信发送失败: phone={}, error={}", 
                    message.getPhone(), e.getMessage(), e);
            
            // 抛出异常触发消息队列重试
            throw new RuntimeException("短信发送失败: " + e.getMessage(), e);
            
        } finally {
            // 保存日志
            this.save(smsLog);
        }
    }

    /**
     * 发送短信的具体实现
     * TODO: 集成实际的短信服务商（阿里云、腾讯云等）
     */
    private void sendSms(SmsMessage message) {
        // 模拟发送短信
        log.info("模拟发送短信: phone={}, content={}, templateCode={}", 
                message.getPhone(), message.getContent(), message.getTemplateCode());
        
        // TODO: 实际对接短信服务商API
        // 例如：阿里云短信服务
        // if (message.getTemplateCode() != null) {
        //     // 使用模板发送
        //     aliyunSmsClient.sendSms(message.getPhone(), message.getTemplateCode(), message.getTemplateParams());
        // } else {
        //     // 直接发送内容
        //     aliyunSmsClient.sendSms(message.getPhone(), message.getContent());
        // }
    }
}
