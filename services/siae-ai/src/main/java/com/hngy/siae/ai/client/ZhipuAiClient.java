package com.hngy.siae.ai.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hngy.siae.ai.config.AiProviderProperties;
import com.hngy.siae.ai.domain.model.ChatMessage;
import com.hngy.siae.ai.domain.model.ChatOptions;
import com.hngy.siae.ai.domain.model.ToolCall;
import com.hngy.siae.ai.exception.AiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * 智谱 AI 客户端实现
 * 支持流式响应、工具调用和思考过程显示
 */
@Slf4j
public class ZhipuAiClient implements LlmClient {

    private static final String PROVIDER_NAME = "zhipu";
    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";
    private static final String SSE_DATA_PREFIX = "data: ";
    private static final String SSE_DONE_MARKER = "[DONE]";

    private final WebClient webClient;
    private final AiProviderProperties.ProviderConfig config;
    private final ObjectMapper objectMapper;
    private final StreamResponseParser responseParser;

    public ZhipuAiClient(AiProviderProperties.ProviderConfig config, 
                         ObjectMapper objectMapper,
                         WebClient.Builder webClientBuilder) {
        this.config = config;
        this.objectMapper = objectMapper;
        this.responseParser = new StreamResponseParser(objectMapper);
        this.webClient = createWebClient(config, webClientBuilder);
        log.info("ZhipuAiClient initialized with base URL: {}", config.getBaseUrl());
    }

    public ZhipuAiClient(AiProviderProperties.ProviderConfig config, ObjectMapper objectMapper) {
        this(config, objectMapper, WebClient.builder());
    }

    private WebClient createWebClient(AiProviderProperties.ProviderConfig config, 
                                       WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(config.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + config.getApiKey())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public Flux<String> chatStream(String model, List<ChatMessage> messages, ChatOptions options) {
        return chatStreamUnified(model, messages, options, null)
                .filter(result -> result.hasContent())
                .map(StreamResponseParser.ParseResult::content);
    }

    @Override
    public Flux<StreamResponseParser.ParseResult> chatStreamUnified(
            String model, 
            List<ChatMessage> messages, 
            ChatOptions options,
            List<Map<String, Object>> tools) {
        
        log.info("Starting unified chat stream with model: {}, messages: {}, tools: {}", 
                model, messages.size(), tools != null ? tools.size() : 0);

        Map<String, Object> requestBody = buildRequestBody(model, messages, options, tools);
        
        try {
            log.debug("Request body: {}", objectMapper.writeValueAsString(requestBody));
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize request body for logging");
        }

        int timeout = options.getResponseTimeout() != null ? options.getResponseTimeout() : 60;

        return webClient.post()
                .uri(CHAT_COMPLETIONS_PATH)
                .bodyValue(requestBody)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)
                .timeout(Duration.ofSeconds(timeout))
                .filter(responseParser::isValidSseData)
                .map(responseParser::parse)
                .onErrorMap(this::mapError)
                .doOnError(e -> log.error("Error in chat stream: {}", e.getMessage()))
                .doOnComplete(() -> log.debug("Chat stream completed"));
    }

    /**
     * 非流式调用（用于工具调用场景）
     */
    public Mono<JsonNode> chatWithTools(String model, List<ChatMessage> messages, 
                                         ChatOptions options, List<Map<String, Object>> tools) {
        log.info("Starting non-stream chat with model: {}, messages: {}, tools: {}", 
                model, messages.size(), tools != null ? tools.size() : 0);

        Map<String, Object> requestBody = buildRequestBody(model, messages, options, tools);
        requestBody.put("stream", false);

        int timeout = options.getResponseTimeout() != null ? options.getResponseTimeout() : 60;

        return webClient.post()
                .uri(CHAT_COMPLETIONS_PATH)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(timeout))
                .map(response -> {
                    try {
                        return objectMapper.readTree(response);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to parse response", e);
                    }
                })
                .onErrorMap(this::mapError);
    }

    /**
     * 解析工具调用
     */
    public List<ToolCall> parseToolCalls(JsonNode response) {
        List<ToolCall> toolCalls = new ArrayList<>();
        
        JsonNode choices = response.get("choices");
        if (choices == null || !choices.isArray() || choices.isEmpty()) {
            return toolCalls;
        }

        JsonNode message = choices.get(0).get("message");
        if (message == null) {
            return toolCalls;
        }

        JsonNode toolCallsNode = message.get("tool_calls");
        if (toolCallsNode == null || !toolCallsNode.isArray()) {
            return toolCalls;
        }

        for (JsonNode tc : toolCallsNode) {
            String id = tc.has("id") ? tc.get("id").asText() : UUID.randomUUID().toString();
            JsonNode function = tc.get("function");
            if (function != null) {
                String name = function.get("name").asText();
                String arguments = function.get("arguments").asText();
                toolCalls.add(new ToolCall(id, name, arguments));
            }
        }

        return toolCalls;
    }

    /**
     * 解析内容响应
     */
    public String parseContent(JsonNode response) {
        JsonNode choices = response.get("choices");
        if (choices == null || !choices.isArray() || choices.isEmpty()) {
            return "";
        }

        JsonNode message = choices.get(0).get("message");
        if (message == null) {
            return "";
        }

        JsonNode content = message.get("content");
        return content != null && !content.isNull() ? content.asText() : "";
    }

    /**
     * 检查是否有工具调用
     */
    public boolean hasToolCalls(JsonNode response) {
        JsonNode choices = response.get("choices");
        if (choices == null || !choices.isArray() || choices.isEmpty()) {
            return false;
        }

        JsonNode finishReason = choices.get(0).get("finish_reason");
        return finishReason != null && "tool_calls".equals(finishReason.asText());
    }

    private Throwable mapError(Throwable error) {
        if (error instanceof TimeoutException) {
            return AiException.responseTimeout();
        }
        
        if (error instanceof WebClientResponseException wcre) {
            int statusCode = wcre.getStatusCode().value();
            if (statusCode == 401) return AiException.apiKeyInvalid(PROVIDER_NAME);
            if (statusCode == 404) return AiException.serviceUnavailable();
            if (statusCode >= 500) return AiException.serviceUnavailable();
        }
        
        return AiException.llmProviderError(error);
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public boolean isAvailable() {
        return config != null && config.isValid();
    }

    @Override
    public String getDisplayName() {
        return config != null && config.getDisplayName() != null ? config.getDisplayName() : "智谱AI";
    }

    private Map<String, Object> buildRequestBody(String model, List<ChatMessage> messages, 
                                                   ChatOptions options, List<Map<String, Object>> tools) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", convertMessages(messages));
        body.put("stream", true);

        if (options.getTemperature() != null) {
            body.put("temperature", options.getTemperature());
        }
        if (options.getMaxTokens() != null) {
            body.put("max_tokens", options.getMaxTokens());
        }
        
        // 添加工具定义
        if (tools != null && !tools.isEmpty()) {
            body.put("tools", tools);
            body.put("tool_choice", "auto");
        }

        return body;
    }

    private List<Map<String, Object>> convertMessages(List<ChatMessage> messages) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (ChatMessage msg : messages) {
            Map<String, Object> msgMap = new LinkedHashMap<>();
            msgMap.put("role", msg.getRole());
            
            if (msg.getContent() != null) {
                msgMap.put("content", msg.getContent());
            }
            
            // 处理工具调用消息（tool 角色）
            if (msg.getToolCallId() != null) {
                msgMap.put("tool_call_id", msg.getToolCallId());
            }
            
            // 处理 assistant 的工具调用
            if (msg.getToolCalls() != null && !msg.getToolCalls().isEmpty()) {
                List<Map<String, Object>> toolCallsList = new ArrayList<>();
                for (var tc : msg.getToolCalls()) {
                    Map<String, Object> tcMap = new LinkedHashMap<>();
                    tcMap.put("id", tc.getId());
                    tcMap.put("type", "function");
                    Map<String, String> funcMap = new LinkedHashMap<>();
                    funcMap.put("name", tc.getToolName());
                    // 优先使用原始 JSON 字符串
                    String args = tc.getArgumentsJson();
                    if (args == null && tc.getParameters() != null) {
                        try {
                            args = objectMapper.writeValueAsString(tc.getParameters());
                        } catch (JsonProcessingException e) {
                            args = "{}";
                        }
                    }
                    funcMap.put("arguments", args != null ? args : "{}");
                    tcMap.put("function", funcMap);
                    toolCallsList.add(tcMap);
                }
                msgMap.put("tool_calls", toolCallsList);
            }
            
            result.add(msgMap);
        }

        return result;
    }

    private boolean isValidSseData(String data) {
        if (data == null || data.isBlank()) return false;
        String trimmed = data.trim();
        return !trimmed.equals(SSE_DONE_MARKER) && !trimmed.equals(SSE_DATA_PREFIX + SSE_DONE_MARKER);
    }

    /**
     * 解析流式响应中的内容
     */
    public String parseStreamContent(String chunk) {
        try {
            String data = chunk.trim();
            if (data.startsWith(SSE_DATA_PREFIX)) {
                data = data.substring(SSE_DATA_PREFIX.length()).trim();
            }
            if (data.isEmpty() || data.equals(SSE_DONE_MARKER)) return "";

            JsonNode root = objectMapper.readTree(data);
            JsonNode choices = root.get("choices");
            
            if (choices != null && choices.isArray() && !choices.isEmpty()) {
                JsonNode delta = choices.get(0).get("delta");
                if (delta != null) {
                    JsonNode content = delta.get("content");
                    if (content != null && !content.isNull()) {
                        return content.asText();
                    }
                }
            }
            return "";
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse stream response: {}", chunk);
            return "";
        }
    }

    /**
     * 解析流式响应中的工具调用
     */
    public List<ToolCall> parseStreamToolCalls(String chunk) {
        List<ToolCall> toolCalls = new ArrayList<>();
        try {
            String data = chunk.trim();
            if (data.startsWith(SSE_DATA_PREFIX)) {
                data = data.substring(SSE_DATA_PREFIX.length()).trim();
            }
            if (data.isEmpty() || data.equals(SSE_DONE_MARKER)) return toolCalls;

            JsonNode root = objectMapper.readTree(data);
            JsonNode choices = root.get("choices");
            
            if (choices != null && choices.isArray() && !choices.isEmpty()) {
                JsonNode delta = choices.get(0).get("delta");
                if (delta != null) {
                    JsonNode tcNode = delta.get("tool_calls");
                    if (tcNode != null && tcNode.isArray()) {
                        for (JsonNode tc : tcNode) {
                            String id = tc.has("id") ? tc.get("id").asText() : "";
                            JsonNode function = tc.get("function");
                            if (function != null) {
                                String name = function.has("name") ? function.get("name").asText() : "";
                                String args = function.has("arguments") ? function.get("arguments").asText() : "";
                                if (!name.isEmpty() || !args.isEmpty()) {
                                    toolCalls.add(new ToolCall(id, name, args));
                                }
                            }
                        }
                    }
                }
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse stream tool calls: {}", chunk);
        }
        return toolCalls;
    }
}
