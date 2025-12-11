package com.hngy.siae.ai.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 聊天请求DTO
 * <p>
 * 用于接收用户发送的聊天消息
 * <p>
 * Requirements: 2.1, 6.1
 *
 * @author SIAE Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    /**
     * 消息内容
     * 不能为空或仅包含空白字符
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /**
     * 会话ID
     * 可选，如果不提供则创建新会话
     */
    private String sessionId;

    /**
     * 模型名称
     * 可选，如果不提供则使用配置文件中的默认模型 (siae.ai.model)
     * 示例: gemma3:12b, qwen2.5:7b
     */
    private String model;

    /**
     * 附加的文件ID列表
     * 可选，用于图片识别等多模态功能
     * 前端上传图片后获得fileId，在发送消息时携带
     */
    private List<String> fileIds;
}
