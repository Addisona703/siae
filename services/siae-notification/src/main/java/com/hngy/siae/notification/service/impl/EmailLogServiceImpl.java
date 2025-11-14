package com.hngy.siae.notification.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.messaging.EmailMessage;
import com.hngy.siae.notification.entity.EmailLog;
import com.hngy.siae.notification.enums.SendStatus;
import com.hngy.siae.notification.mapper.EmailLogMapper;
import com.hngy.siae.notification.service.EmailLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;

/**
 * 邮件日志服务实现类
 *
 * @author KEYKB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailLogServiceImpl extends ServiceImpl<EmailLogMapper, EmailLog> implements EmailLogService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public void handleEmailMessage(EmailMessage message) {
        log.info("开始处理邮件消息: recipient={}, subject={}", 
                message.getRecipient(), message.getSubject());

        // 创建邮件日志记录
        EmailLog emailLog = new EmailLog();
        emailLog.setRecipient(message.getRecipient());
        emailLog.setSubject(message.getSubject());
        emailLog.setContent(message.getContent());
        emailLog.setStatus(SendStatus.PENDING);

        try {
            // 发送邮件
            sendEmail(message);

            // 更新状态为成功
            emailLog.setStatus(SendStatus.SUCCESS);
            emailLog.setSendTime(LocalDateTime.now());
            log.info("邮件发送成功: recipient={}", message.getRecipient());

        } catch (Exception e) {
            // 更新状态为失败
            emailLog.setStatus(SendStatus.FAILED);
            emailLog.setErrorMsg(e.getMessage());
            log.error("邮件发送失败: recipient={}, error={}", 
                    message.getRecipient(), e.getMessage(), e);
            
            // 抛出异常触发消息队列重试
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
            
        } finally {
            // 保存日志
            this.save(emailLog);
        }
    }

    /**
     * 发送邮件
     */
    private void sendEmail(EmailMessage message) throws Exception {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        
        helper.setFrom(from);
        helper.setTo(message.getRecipient());
        helper.setSubject(message.getSubject());
        
        // 支持HTML格式
        helper.setText(message.getContent(), true);
        
        mailSender.send(mimeMessage);
    }
}
