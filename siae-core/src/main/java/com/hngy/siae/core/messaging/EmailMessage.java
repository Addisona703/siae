package com.hngy.siae.core.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 邮件消息实体（用于跨服务消息传递）
 * 
 * @author KEYKB
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 收件人邮箱
     */
    private String recipient;

    /**
     * 邮件主题
     */
    private String subject;

    /**
     * 邮件内容（支持HTML）
     */
    private String content;

    /**
     * 模板代码（可选，使用模板发送）
     */
    private String templateCode;

    /**
     * 模板参数（可选）
     */
    private Map<String, Object> templateParams;

    /**
     * 业务ID（可选，用于追踪）
     */
    private Long businessId;

    /**
     * 业务类型（可选）
     */
    private String businessType;
}
