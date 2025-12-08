package com.hngy.siae.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录验证事件消息
 * <p>
 * 用于用户登录时触发邮箱验证码发送
 * 
 * @author KEYKB
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginVerificationEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 事件类型
     * LOGIN_VERIFICATION - 登录验证
     * REGISTER_VERIFICATION - 注册验证
     * PASSWORD_RESET - 密码重置
     */
    private String eventType;

    /**
     * 客户端IP（可选，用于安全日志）
     */
    private String clientIp;

    /**
     * 浏览器信息（可选）
     */
    private String browser;

    /**
     * 操作系统信息（可选）
     */
    private String os;
}
