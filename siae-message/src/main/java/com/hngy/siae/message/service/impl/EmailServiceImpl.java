package com.hngy.siae.message.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.hngy.siae.message.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;

    private static final String EMAIL_CODE_PREFIX = "email:code:";
    private static final int CODE_LENGTH = 6;
    private static final int EXPIRE_MINUTES = 5;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public boolean sendVerificationCode(String to, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("【软协官网】邮箱验证码");
            message.setText(String.format(
                "您的验证码是：%s\n\n" +
                "验证码5分钟内有效，请勿泄露给他人。\n\n" +
                "如非本人操作，请忽略此邮件。", code));
            
            mailSender.send(message);
            log.info("验证码邮件发送成功，收件人：{}", to);
            return true;
        } catch (Exception e) {
            log.error("验证码邮件发送失败，收件人：{}，错误信息：{}", to, e.getMessage());
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