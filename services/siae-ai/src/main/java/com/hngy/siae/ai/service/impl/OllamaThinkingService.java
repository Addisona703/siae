package com.hngy.siae.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hngy.siae.ai.config.AiProperties;
import com.hngy.siae.ai.tool.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.*;

/**
 * Ollama Thinking 服务
 * 直接调用 Ollama API 以支持 think 参数和工具调用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OllamaThinkingService {

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    
    // 工具类注入
    private final WeatherTool weatherTool;
    private final MemberQueryTool memberQueryTool;
    private final AwardQueryTool awardQueryTool;
    private final ContentQueryTool contentQueryTool;
    
    private WebClient webClient;

    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = WebClient.builder()
                    .baseUrl(aiProperties.getBaseUrl())
                    .build();
        }
        return webClient;
    }

    /**
     * 流式聊天（支持 thinking + 工具调用）
     * @param messages 消息列表
     * @param model 模型名称，如果为null则使用默认模型
     */
    public Flux<ThinkingChunk> chatStreamWithThinking(List<Map<String, Object>> messages, String model) {
        String effectiveModel = (model != null && !model.isBlank()) ? model : aiProperties.getModel();
        return doChat(messages, true, effectiveModel);
    }

    private Flux<ThinkingChunk> doChat(List<Map<String, Object>> messages, boolean isFirstCall, String model) {
        // 检查消息中是否包含图片
        boolean hasImages = messages.stream()
                .anyMatch(msg -> msg.containsKey("images") && msg.get("images") != null);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("stream", true);
        
        // 如果有图片，使用普通流式传输（不使用 think 和 tools）
        if (hasImages) {
            log.info("Calling Ollama API with images (normal streaming), model: {}, messages: {}", model, messages.size());
        } else {
            // 没有图片时才使用 think 和 tools
            requestBody.put("think", true);
            requestBody.put("tools", buildToolDefinitions());
            log.info("Calling Ollama API with thinking + tools, model: {}, messages: {}", model, messages.size());
        }

        try {
            String requestJson = objectMapper.writeValueAsString(requestBody);
            log.debug("Request body: {}", requestJson);
        } catch (Exception e) {
            log.error("Failed to serialize request body", e);
        }

        // 收集流式响应
        StringBuilder thinkingBuilder = new StringBuilder();
        StringBuilder contentBuilder = new StringBuilder();
        List<Map<String, Object>> toolCalls = new ArrayList<>();

        return getWebClient().post()
                .uri("/api/chat")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .timeout(Duration.ofSeconds(aiProperties.getResponseTimeout()))
                .filter(line -> !line.isBlank())
                .concatMap(json -> {
                    ThinkingChunk chunk = parseChunk(json, thinkingBuilder, contentBuilder, toolCalls);
                    if (chunk == null) {
                        return Flux.empty();
                    }
                    
                    // 如果是最后一个chunk且有工具调用
                    if (chunk.isDone() && !toolCalls.isEmpty()) {
                        log.info("Tool calls detected: {}", toolCalls.size());
                        return Flux.just(chunk)
                                .concatWith(handleToolCalls(messages, thinkingBuilder.toString(), 
                                        contentBuilder.toString(), toolCalls, model));
                    }
                    
                    return Flux.just(chunk);
                });
    }


    /**
     * 处理工具调用（在独立线程池中执行阻塞操作）
     */
    private Flux<ThinkingChunk> handleToolCalls(List<Map<String, Object>> originalMessages,
                                                  String thinking, String content,
                                                  List<Map<String, Object>> toolCalls,
                                                  String model) {
        return Mono.fromCallable(() -> {
            // 构建新的消息列表
            List<Map<String, Object>> newMessages = new ArrayList<>(originalMessages);
            
            // 添加助手消息（包含工具调用）
            Map<String, Object> assistantMsg = new HashMap<>();
            assistantMsg.put("role", "assistant");
            assistantMsg.put("content", content);
            if (!thinking.isEmpty()) {
                assistantMsg.put("thinking", thinking);
            }
            assistantMsg.put("tool_calls", toolCalls);
            newMessages.add(assistantMsg);
            
            // 收集工具执行结果
            List<ThinkingChunk> chunks = new ArrayList<>();
            
            for (Map<String, Object> toolCall : toolCalls) {
                @SuppressWarnings("unchecked")
                Map<String, Object> function = (Map<String, Object>) toolCall.get("function");
                String toolName = (String) function.get("name");
                @SuppressWarnings("unchecked")
                Map<String, Object> arguments = (Map<String, Object>) function.get("arguments");
                
                log.info("Executing tool: {} with arguments: {}", toolName, arguments);
                
                // 工具调用通知
                ThinkingChunk toolCallChunk = new ThinkingChunk();
                toolCallChunk.setToolCall(toolName);
                chunks.add(toolCallChunk);
                
                // 执行工具（阻塞操作）
                String result = executeTool(toolName, arguments);
                
                // 添加工具结果消息
                Map<String, Object> toolResultMsg = new HashMap<>();
                toolResultMsg.put("role", "tool");
                toolResultMsg.put("content", result);
                newMessages.add(toolResultMsg);
                
                // 工具结果通知
                ThinkingChunk toolResultChunk = new ThinkingChunk();
                toolResultChunk.setToolResult(result);
                chunks.add(toolResultChunk);
            }
            
            return new ToolExecutionResult(chunks, newMessages);
        })
        .subscribeOn(Schedulers.boundedElastic())  // 在独立线程池执行阻塞操作
        .flatMapMany(result -> 
            Flux.fromIterable(result.chunks())
                .concatWith(Flux.defer(() -> doChat(result.newMessages(), false, model)))
        );
    }
    
    /**
     * 工具执行结果
     */
    private record ToolExecutionResult(List<ThinkingChunk> chunks, List<Map<String, Object>> newMessages) {}

    /**
     * 执行工具
     */
    private String executeTool(String toolName, Map<String, Object> arguments) {
        try {
            Object result = switch (toolName) {
                case "getWeather" -> weatherTool.getWeather(
                        getStringArg(arguments, "city"));
                
                case "queryMembers" -> memberQueryTool.queryMembers(
                        getStringArg(arguments, "name"),
                        getStringArg(arguments, "department"),
                        getStringArg(arguments, "position"));
                
                case "getMemberStatistics" -> memberQueryTool.getMemberStatistics();
                
                case "queryMemberAwards" -> awardQueryTool.queryMemberAwards(
                        getStringArg(arguments, "memberName"),
                        getStringArg(arguments, "studentId"));
                
                case "getAwardStatistics" -> awardQueryTool.getAwardStatistics(
                        getLongArg(arguments, "typeId"),
                        getLongArg(arguments, "levelId"),
                        getStringArg(arguments, "startDate"),
                        getStringArg(arguments, "endDate"));
                
                case "searchContent" -> contentQueryTool.searchContent(
                        getStringArg(arguments, "keyword"),
                        getStringArg(arguments, "categoryName"),
                        getIntArg(arguments));
                
                case "getHotContent" -> contentQueryTool.getHotContent(
                        getIntArg(arguments));
                
                case "getLatestContent" -> contentQueryTool.getLatestContent(
                        getIntArg(arguments));
                
                default -> "未知工具: " + toolName;
            };
            
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            log.error("Tool execution failed: {} - {}", toolName, e.getMessage(), e);
            return "工具执行失败: " + e.getMessage();
        }
    }

    private String getStringArg(Map<String, Object> args, String key) {
        Object value = args.get(key);
        return value != null ? value.toString() : null;
    }

    private Long getLongArg(Map<String, Object> args, String key) {
        Object value = args.get(key);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer getIntArg(Map<String, Object> args) {
        Object value = args.get("limit");
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }


    /**
     * 构建工具定义
     */
    private List<Map<String, Object>> buildToolDefinitions() {
        List<Map<String, Object>> tools = new ArrayList<>();
        
        // getWeather
        tools.add(buildTool("getWeather", 
                "查询指定城市的天气信息，包括温度、天气状况、湿度、风速等。支持中文城市名。",
                Map.of("city", Map.of("type", "string", "description", "城市名称，如：北京、上海、广州")),
                List.of("city")));
        
        // queryMembers
        tools.add(buildTool("queryMembers",
                "查询成员信息，可按姓名、部门、职位查询。返回成员的基本信息。",
                Map.of(
                        "name", Map.of("type", "string", "description", "成员姓名，支持模糊匹配"),
                        "department", Map.of("type", "string", "description", "部门名称，如：技术部、宣传部"),
                        "position", Map.of("type", "string", "description", "职位名称，如：部长、副部长、干事")
                ),
                List.of()));
        
        // getMemberStatistics
        tools.add(buildTool("getMemberStatistics",
                "获取成员统计数据，包括成员总数、各部门人数、各年级分布等。",
                Map.of(),
                List.of()));
        
        // queryMemberAwards
        tools.add(buildTool("queryMemberAwards",
                "查询指定成员的获奖记录，可按成员姓名、学号查询。",
                Map.of(
                        "memberName", Map.of("type", "string", "description", "成员姓名，支持模糊匹配"),
                        "studentId", Map.of("type", "string", "description", "学号，精确匹配")
                ),
                List.of()));
        
        // getAwardStatistics
        tools.add(buildTool("getAwardStatistics",
                "查询获奖统计信息，可按奖项类型、等级、时间范围统计。",
                Map.of(
                        "typeId", Map.of("type", "integer", "description", "奖项类型ID"),
                        "levelId", Map.of("type", "integer", "description", "奖项等级ID"),
                        "startDate", Map.of("type", "string", "description", "开始日期，格式yyyy-MM-dd"),
                        "endDate", Map.of("type", "string", "description", "结束日期，格式yyyy-MM-dd")
                ),
                List.of()));
        
        // searchContent
        tools.add(buildTool("searchContent",
                "搜索内容，可按关键词、分类搜索文章、视频等内容。",
                Map.of(
                        "keyword", Map.of("type", "string", "description", "搜索关键词"),
                        "categoryName", Map.of("type", "string", "description", "分类名称"),
                        "limit", Map.of("type", "integer", "description", "返回数量限制，默认10")
                ),
                List.of()));
        
        // getHotContent
        tools.add(buildTool("getHotContent",
                "获取热门内容，返回浏览量最高的内容列表。",
                Map.of("limit", Map.of("type", "integer", "description", "返回数量限制，默认10")),
                List.of()));
        
        // getLatestContent
        tools.add(buildTool("getLatestContent",
                "获取最新发布的内容列表。",
                Map.of("limit", Map.of("type", "integer", "description", "返回数量限制，默认10")),
                List.of()));
        
        return tools;
    }

    private Map<String, Object> buildTool(String name, String description, 
                                           Map<String, Object> properties, List<String> required) {
        Map<String, Object> tool = new HashMap<>();
        tool.put("type", "function");
        
        Map<String, Object> function = new HashMap<>();
        function.put("name", name);
        function.put("description", description);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        parameters.put("properties", properties);
        // 只在有必需参数时才添加 required 字段
        if (required != null && !required.isEmpty()) {
            parameters.put("required", required);
        }
        
        function.put("parameters", parameters);
        tool.put("function", function);
        
        return tool;
    }


    /**
     * 解析响应块
     */
    private ThinkingChunk parseChunk(String json, StringBuilder thinkingBuilder, 
                                      StringBuilder contentBuilder, List<Map<String, Object>> toolCalls) {
        try {
            JsonNode node = objectMapper.readTree(json);
            JsonNode messageNode = node.get("message");
            if (messageNode == null) {
                return null;
            }

            ThinkingChunk chunk = new ThinkingChunk();
            
            // 解析 thinking 内容
            if (messageNode.has("thinking") && !messageNode.get("thinking").isNull()) {
                String thinking = messageNode.get("thinking").asText();
                if (!thinking.isEmpty()) {
                    chunk.setThinking(thinking);
                    thinkingBuilder.append(thinking);
                }
            }
            
            // 解析 content 内容
            if (messageNode.has("content") && !messageNode.get("content").isNull()) {
                String content = messageNode.get("content").asText();
                if (!content.isEmpty()) {
                    chunk.setContent(content);
                    contentBuilder.append(content);
                }
            }
            
            // 解析 tool_calls
            if (messageNode.has("tool_calls") && messageNode.get("tool_calls").isArray()) {
                for (JsonNode toolCallNode : messageNode.get("tool_calls")) {
                    Map<String, Object> toolCall = new HashMap<>();
                    
                    JsonNode functionNode = toolCallNode.get("function");
                    if (functionNode != null) {
                        Map<String, Object> function = new HashMap<>();
                        function.put("name", functionNode.get("name").asText());
                        
                        // 解析参数
                        JsonNode argsNode = functionNode.get("arguments");
                        if (argsNode != null) {
                            if (argsNode.isObject()) {
                                function.put("arguments", objectMapper.convertValue(argsNode, Map.class));
                            } else if (argsNode.isTextual()) {
                                // 有时候参数是字符串形式的JSON
                                function.put("arguments", objectMapper.readValue(argsNode.asText(), Map.class));
                            }
                        } else {
                            function.put("arguments", new HashMap<>());
                        }
                        
                        toolCall.put("function", function);
                        toolCalls.add(toolCall);
                    }
                }
            }
            
            // 检查是否完成
            if (node.has("done")) {
                chunk.setDone(node.get("done").asBoolean());
            }

            return chunk;
        } catch (Exception e) {
            log.warn("Failed to parse chunk: {}", json, e);
            return null;
        }
    }

    /**
     * 构建消息列表
     */
    public List<Map<String, Object>> buildMessages(String systemPrompt, 
                                                    List<Map<String, Object>> history, 
                                                    String userMessage) {
        return buildMessages(systemPrompt, history, userMessage, null);
    }
    
    /**
     * 构建消息列表（支持图片）
     * @param systemPrompt 系统提示
     * @param history 历史消息
     * @param userMessage 用户消息
     * @param imageBase64List 图片的 Base64 编码列表
     */
    public List<Map<String, Object>> buildMessages(String systemPrompt, 
                                                    List<Map<String, Object>> history, 
                                                    String userMessage,
                                                    List<String> imageBase64List) {
        List<Map<String, Object>> messages = new ArrayList<>();
        
        // 系统提示
        Map<String, Object> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);
        
        // 历史消息
        if (history != null) {
            messages.addAll(history);
        }
        
        // 用户消息
        Map<String, Object> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        
        // 如果有图片，添加到消息中
        if (imageBase64List != null && !imageBase64List.isEmpty()) {
            userMsg.put("images", imageBase64List);
            log.info("Added {} images to user message", imageBase64List.size());
        }
        
        messages.add(userMsg);
        
        return messages;
    }

    /**
     * Thinking 响应块
     */
    @Setter
    @Getter
    public static class ThinkingChunk {
        private String thinking;
        private String content;
        private String toolCall;
        private String toolResult;
        private boolean done;

        public boolean hasThinking() { return thinking != null && !thinking.isEmpty(); }
        public boolean hasContent() { return content != null && !content.isEmpty(); }
        public boolean hasToolCall() { return toolCall != null && !toolCall.isEmpty(); }
        public boolean hasToolResult() { return toolResult != null && !toolResult.isEmpty(); }
    }
}
