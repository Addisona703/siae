package com.hngy.siae.message.service;

public interface EmailService {
    
    /**
     * 发送验证码邮件
     * @param to 收件人邮箱
     * @param code 验证码
     * @return 是否发送成功
     */
    boolean sendVerificationCode(String to, String code);

    /**
     * 发送邮箱验证码
     * @param email 邮箱地址
     * @return 是否发送成功
     */
    boolean sendEmailCode(String email);
    
    /**
     * 验证邮箱验证码
     * @param email 邮箱地址
     * @param code 验证码
     * @return 是否验证通过
     */
    boolean verifyEmailCode(String email, String code);
}