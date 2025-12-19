package com.hngy.siae.ai.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hngy.siae.ai.domain.model.ToolCall;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 流式响应解析器
 * 统一解析不同模型的流式响应，支持内容、思考过程和工具调用
 */
@Slf4j
@RequiredArgsConstructor
public class StreamResponseParser {

    private static final String SSE_DATA_PREFIX = "data: ";
    private static final String SSE_DONE_MARKER = "[DONE]";

    private final ObjectMapper objectMapper;

    /**
     * 解析结果
     */
    public record ParseResult(
            String content,           // 普通内容
            String reasoning,         // 思考过程（reasoning_content）
            List<ToolCallDelta> toolCallDeltas,  // 工具调用增量
            String finishReason       // 结束原因
    ) {
        public boolean hasContent() {
            return content != null && !content.isEmpty();
        }

        public boolean hasReasoning() {
            return reasoning != null && !reasoning.isEmpty();
        }

        public boolean hasToolCalls() {
            return toolCallDeltas != null && !toolCallDeltas.isEmpty();
        }

        public boolean isToolCallFinish() {
            return "tool_calls".equals(finishReason);
        }

        public boolean isStopFinish() {
            return "stop".equals(finishReason);
        }
    }

    /**
     * 工具调用增量数据
     */
    public record ToolCallDelta(
            int index,
            String id,
            String name,
            String argumentsDelta
    ) {}

    /**
     * 解析 SSE 数据块
     */
    public ParseResult parse(String chunk) {
        String data = extractData(chunk);
        if (data == null || data.isEmpty()) {
            return new ParseResult(null, null, null, null);
        }

        try {
            JsonNode root = objectMapper.readTree(data);
            JsonNode choices = root.get("choices");

            if (choices == null || !choices.isArray() || choices.isEmpty()) {
                return new ParseResult(null, null, null, null);
            }

            JsonNode choice = choices.get(0);
            JsonNode delta = choice.get("delta");
            String finishReason = getTextValue(choice, "finish_reason");

            if (delta == null) {
                return new ParseResult(null, null, null, finishReason);
            }

            // 解析普通内容
            String content = getTextValue(delta, "content");

            // 解析思考过程（DeepSeek 等模型的 reasoning_content）
            String reasoning = getTextValue(delta, "reasoning_content");

            // 解析工具调用
            List<ToolCallDelta> toolCallDeltas = parseToolCallDeltas(delta);

            return new ParseResult(content, reasoning, toolCallDeltas, finishReason);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse stream chunk: {}", chunk);
            return new ParseResult(null, null, null, null);
        }
    }

    /**
     * 从 SSE 数据中提取 JSON 部分
     */
    private String extractData(String chunk) {
        if (chunk == null || chunk.isBlank()) {
            return null;
        }

        String data = chunk.trim();
        if (data.startsWith(SSE_DATA_PREFIX)) {
            data = data.substring(SSE_DATA_PREFIX.length()).trim();
        }

        if (data.isEmpty() || data.equals(SSE_DONE_MARKER)) {
            return null;
        }

        return data;
    }

    /**
     * 安全获取文本值
     */
    private String getTextValue(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode != null && !fieldNode.isNull()) {
            return fieldNode.asText();
        }
        return null;
    }

    /**
     * 解析工具调用增量
     */
    private List<ToolCallDelta> parseToolCallDeltas(JsonNode delta) {
        List<ToolCallDelta> deltas = new ArrayList<>();
        JsonNode toolCallsNode = delta.get("tool_calls");

        if (toolCallsNode == null || !toolCallsNode.isArray()) {
            return deltas;
        }

        for (JsonNode tc : toolCallsNode) {
            int index = tc.has("index") ? tc.get("index").asInt() : 0;
            String id = getTextValue(tc, "id");

            JsonNode function = tc.get("function");
            String name = null;
            String args = null;

            if (function != null) {
                name = getTextValue(function, "name");
                args = getTextValue(function, "arguments");
            }

            if (id != null || name != null || args != null) {
                deltas.add(new ToolCallDelta(index, id, name, args));
            }
        }

        return deltas;
    }

    /**
     * 检查是否为有效的 SSE 数据
     */
    public boolean isValidSseData(String data) {
        if (data == null || data.isBlank()) {
            return false;
        }
        String trimmed = data.trim();
        return !trimmed.equals(SSE_DONE_MARKER) && !trimmed.equals(SSE_DATA_PREFIX + SSE_DONE_MARKER);
    }
}
