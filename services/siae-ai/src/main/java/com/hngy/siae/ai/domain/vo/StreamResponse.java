package com.hngy.siae.ai.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 流式响应数据结构
 * <p>
 * 用于SSE流式聊天的响应格式，支持内容、思考过程、工具调用、错误和完成等类型。
 * 统一接口支持普通聊天、工具调用和思考模型。
 * <p>
 * Requirements: 4.5, 9.4
 *
 * @author SIAE Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StreamResponse {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 响应类型: content | thinking | tool_call | tool_result | error | done
     */
    private String type;

    /**
     * 响应文本内容
     */
    private String text;

    /**
     * 是否为最终响应
     */
    private boolean isFinal;

    /**
     * 错误码（仅在type=error时有值）
     */
    private Integer errorCode;

    /**
     * 工具调用信息（仅在type=tool_call时有值）
     */
    private ToolCallData toolCall;

    /**
     * 工具执行结果（仅在type=tool_result时有值）
     */
    private ToolResultData toolResult;

    /**
     * 响应类型常量
     */
    public static final String TYPE_CONTENT = "content";
    public static final String TYPE_THINKING = "thinking";
    public static final String TYPE_TOOL_CALL = "tool_call";
    public static final String TYPE_TOOL_RESULT = "tool_result";
    public static final String TYPE_ERROR = "error";
    public static final String TYPE_DONE = "done";

    /**
     * 工具调用数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolCallData {
        private String id;
        private String name;
        private Map<String, Object> arguments;
    }

    /**
     * 工具执行结果数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolResultData {
        private String toolCallId;
        private String toolName;
        private Object result;
        private boolean success;
        private String error;
    }

    /**
     * 创建内容响应
     */
    public static StreamResponse content(String sessionId, String text) {
        return StreamResponse.builder()
                .sessionId(sessionId)
                .type(TYPE_CONTENT)
                .text(text)
                .isFinal(false)
                .build();
    }

    /**
     * 创建思考过程响应（用于支持 reasoning 的模型如 DeepSeek）
     */
    public static StreamResponse thinking(String sessionId, String text) {
        return StreamResponse.builder()
                .sessionId(sessionId)
                .type(TYPE_THINKING)
                .text(text)
                .isFinal(false)
                .build();
    }

    /**
     * 创建工具调用响应
     */
    public static StreamResponse toolCall(String sessionId, String id, String name, Map<String, Object> arguments) {
        return StreamResponse.builder()
                .sessionId(sessionId)
                .type(TYPE_TOOL_CALL)
                .toolCall(ToolCallData.builder()
                        .id(id)
                        .name(name)
                        .arguments(arguments)
                        .build())
                .isFinal(false)
                .build();
    }

    /**
     * 创建工具执行结果响应
     */
    public static StreamResponse toolResult(String sessionId, String toolCallId, String toolName, 
                                             Object result, boolean success, String error) {
        return StreamResponse.builder()
                .sessionId(sessionId)
                .type(TYPE_TOOL_RESULT)
                .toolResult(ToolResultData.builder()
                        .toolCallId(toolCallId)
                        .toolName(toolName)
                        .result(result)
                        .success(success)
                        .error(error)
                        .build())
                .isFinal(false)
                .build();
    }

    /**
     * 创建完成响应
     */
    public static StreamResponse done(String sessionId) {
        return StreamResponse.builder()
                .sessionId(sessionId)
                .type(TYPE_DONE)
                .text("")
                .isFinal(true)
                .build();
    }

    /**
     * 创建错误响应
     */
    public static StreamResponse error(String sessionId, String errorMessage) {
        return StreamResponse.builder()
                .sessionId(sessionId)
                .type(TYPE_ERROR)
                .text(errorMessage)
                .isFinal(true)
                .build();
    }

    /**
     * 创建错误响应（带错误码）
     */
    public static StreamResponse error(String sessionId, Integer errorCode, String errorMessage) {
        return StreamResponse.builder()
                .sessionId(sessionId)
                .type(TYPE_ERROR)
                .text(errorMessage)
                .errorCode(errorCode)
                .isFinal(true)
                .build();
    }

    /**
     * 转换为JSON字符串
     */
    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            // 降级处理：手动构建JSON
            return buildFallbackJson();
        }
    }

    /**
     * 降级JSON构建（当Jackson序列化失败时使用）
     */
    private String buildFallbackJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"sessionId\":\"").append(escapeJson(sessionId)).append("\",");
        sb.append("\"type\":\"").append(escapeJson(type)).append("\",");
        sb.append("\"text\":\"").append(escapeJson(text)).append("\",");
        sb.append("\"isFinal\":").append(isFinal);
        if (errorCode != null) {
            sb.append(",\"errorCode\":").append(errorCode);
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * 转义JSON特殊字符
     */
    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
