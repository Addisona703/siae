package com.hngy.siae.notification.controller;

import com.hngy.siae.core.messaging.EmailMessage;
import com.hngy.siae.core.messaging.MessagingConstants;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.messaging.producer.SiaeMessagingTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器 - 用于测试消息发送
 *
 * @author KEYKB
 */
@Slf4j
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Tag(name = "测试接口", description = "用于测试消息队列功能")
public class TestController {

    private final SiaeMessagingTemplate messagingTemplate;

    @PostMapping("/send-email")
    @Operation(summary = "发送测试邮件消息")
    public Result<String> sendTestEmail(@RequestParam(defaultValue = "test@example.com") String recipient) {
        EmailMessage message = new EmailMessage();
        message.setRecipient(recipient);
        message.setSubject("测试邮件 - " + System.currentTimeMillis());
        message.setContent("这是一封测试邮件，发送时间: " + new java.util.Date());
        
        try {
            messagingTemplate.send(
                MessagingConstants.NOTIFICATION_EXCHANGE,
                "email.test",
                message
            );
            log.info("测试邮件消息已发送到队列: recipient={}", recipient);
            return Result.success("邮件消息已发送到队列，请查看日志");
        } catch (Exception e) {
            log.error("发送测试邮件失败", e);
            return Result.fail("发送失败: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查")
    public Result<Map<String, Object>> health() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "siae-notification");
        info.put("status", "UP");
        info.put("timestamp", System.currentTimeMillis());
        return Result.success(info);
    }
}
