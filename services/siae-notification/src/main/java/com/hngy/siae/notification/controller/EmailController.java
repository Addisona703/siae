package com.hngy.siae.notification.controller;

import com.hngy.siae.core.result.Result;
import com.hngy.siae.notification.dto.request.EmailDTO;
import com.hngy.siae.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 邮件控制器
 */
@Slf4j
@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    
    @PostMapping("/code/send")
    public Result<?> sendEmailCode(
            @RequestParam String email) {
        log.info("发送邮箱验证码，邮箱：{}", email);
        boolean result = emailService.sendEmailCode(email);
        return result ? Result.success("验证码发送成功") : Result.error("验证码发送失败");
    }
    
    @PostMapping("/code/verify")
    public Result<?> verifyEmailCode(@RequestBody @Validated EmailDTO emailDTO) {
        log.info("验证邮箱验证码，邮箱：{}", emailDTO.getEmail());
        boolean result = emailService.verifyEmailCode(emailDTO.getEmail(), emailDTO.getCode());
        return result ? Result.success("验证码验证成功") : Result.error("验证码错误或已过期");
    }
}