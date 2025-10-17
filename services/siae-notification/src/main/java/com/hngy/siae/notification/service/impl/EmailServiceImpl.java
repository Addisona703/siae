package com.hngy.siae.notification.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.hngy.siae.notification.service.EmailService;
import com.hngy.siae.notification.service.EmailTemplateService;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 邮件服务实现类
 * 用途：提供邮件发送功能，使用FreeMarker模板渲染邮件内容
 *
 * @author KEYKB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;
    private final EmailTemplateService emailTemplateService;

    private static final String EMAIL_CODE_PREFIX = "email:code:";
    private static final int CODE_LENGTH = 6;
    private static final int EXPIRE_MINUTES = 5;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public boolean sendVerificationCode(String to, String code) {
        try {
            // 使用FreeMarker模板渲染邮件内容
            String htmlContent = emailTemplateService.renderRegisterCodeEmail(
                    null,           // userName - 注册时可能还没有用户名
                    to,             // email
                    code,           // 验证码
                    EXPIRE_MINUTES  // 有效期
            );

            // 创建HTML邮件
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("【SIAE Studio】邮箱验证码");
            helper.setText(htmlContent, true); // true表示HTML格式

            mailSender.send(mimeMessage);
            log.info("验证码邮件发送成功，收件人：{}", to);
            return true;
        } catch (MessagingException | IOException | TemplateException e) {
            log.error("验证码邮件发送失败，收件人：{}，错误信息：{}", to, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendEmailCode(String email) {
        // 生成6位数字验证码
        String code = RandomUtil.randomNumbers(CODE_LENGTH);

        // 发送邮件
        boolean sent = sendVerificationCode(email, code);
        if (sent) {
            // 存储到Redis，5分钟过期
            String key = EMAIL_CODE_PREFIX + email;
            redisTemplate.opsForValue().set(key, code, EXPIRE_MINUTES, TimeUnit.MINUTES);
            log.info("邮箱验证码已发送并缓存，邮箱：{}，验证码：{}", email, code);
        }

        return sent;
    }

    @Override
    public boolean verifyEmailCode(String email, String code) {
        String key = EMAIL_CODE_PREFIX + email;
        String cachedCode = redisTemplate.opsForValue().get(key);

        if (cachedCode != null && cachedCode.equals(code)) {
            // 验证成功后删除验证码
            redisTemplate.delete(key);
            log.info("邮箱验证码验证成功，邮箱：{}", email);
            return true;
        }

        log.warn("邮箱验证码验证失败，邮箱：{}，输入验证码：{}", email, code);
        return false;
    }
}