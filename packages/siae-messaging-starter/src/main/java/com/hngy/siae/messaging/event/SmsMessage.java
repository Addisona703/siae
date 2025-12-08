package com.hngy.siae.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 短信消息实体（用于跨服务消息传递）
 * 
 * @author KEYKB
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 短信内容
     */
    private String content;

    /**
     * 模板代码（可选）
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
