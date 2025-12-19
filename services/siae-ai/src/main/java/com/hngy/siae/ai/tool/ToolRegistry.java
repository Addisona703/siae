package com.hngy.siae.ai.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具注册中心
 * 扫描所有带 @Tool 注解的方法，生成工具定义供 LLM 调用
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ToolRegistry {

    private final List<Object> toolBeans;
    private final ObjectMapper objectMapper;

    private final Map<String, ToolDefinition> tools = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        for (Object bean : toolBeans) {
            registerToolsFromBean(bean);
        }
        log.info("ToolRegistry initialized with {} tools: {}", tools.size(), tools.keySet());
    }

    private void registerToolsFromBean(Object bean) {
        Class<?> clazz = bean.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            Tool toolAnnotation = method.getAnnotation(Tool.class);
            if (toolAnnotation != null) {
                String toolName = method.getName();
                ToolDefinition definition = new ToolDefinition(
                        toolName,
                        toolAnnotation.description(),
                        method,
                        bean,
                        buildParameters(method)
                );
                tools.put(toolName, definition);
                log.debug("Registered tool: {} - {}", toolName, toolAnnotation.description());
            }
        }
    }

    private List<ToolParameter> buildParameters(Method method) {
        List<ToolParameter> params = new ArrayList<>();
        for (Parameter param : method.getParameters()) {
            ToolParam annotation = param.getAnnotation(ToolParam.class);
            String description = annotation != null ? annotation.description() : "";
            boolean required = annotation == null || annotation.required();
            params.add(new ToolParameter(param.getName(), param.getType(), description, required));
        }
        return params;
    }

    /**
     * 获取所有工具定义（用于发送给 LLM）
     */
    public List<Map<String, Object>> getToolDefinitionsForLlm() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ToolDefinition tool : tools.values()) {
            result.add(buildToolDefinitionMap(tool));
        }
        return result;
    }

    private Map<String, Object> buildToolDefinitionMap(ToolDefinition tool) {
        Map<String, Object> toolMap = new LinkedHashMap<>();
        toolMap.put("type", "function");

        Map<String, Object> function = new LinkedHashMap<>();
        function.put("name", tool.name());
        function.put("description", tool.description());

        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("type", "object");

        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        for (ToolParameter param : tool.parameters()) {
            Map<String, Object> paramDef = new LinkedHashMap<>();
            paramDef.put("type", getJsonType(param.type()));
            paramDef.put("description", param.description());
            properties.put(param.name(), paramDef);
            if (param.required()) {
                required.add(param.name());
            }
        }

        parameters.put("properties", properties);
        if (!required.isEmpty()) {
            parameters.put("required", required);
        }

        function.put("parameters", parameters);
        toolMap.put("function", function);

        return toolMap;
    }

    private String getJsonType(Class<?> type) {
        if (type == String.class) return "string";
        if (type == Integer.class || type == int.class) return "integer";
        if (type == Long.class || type == long.class) return "integer";
        if (type == Double.class || type == double.class) return "number";
        if (type == Float.class || type == float.class) return "number";
        if (type == Boolean.class || type == boolean.class) return "boolean";
        return "string";
    }

    /**
     * 执行工具调用
     */
    public Object executeTool(String toolName, Map<String, Object> arguments) {
        ToolDefinition tool = tools.get(toolName);
        if (tool == null) {
            throw new IllegalArgumentException("Unknown tool: " + toolName);
        }

        try {
            Method method = tool.method();
            Object[] args = buildMethodArguments(method, arguments);
            return method.invoke(tool.instance(), args);
        } catch (Exception e) {
            log.error("Error executing tool {}: {}", toolName, e.getMessage(), e);
            throw new RuntimeException("Tool execution failed: " + e.getMessage(), e);
        }
    }

    private Object[] buildMethodArguments(Method method, Map<String, Object> arguments) {
        Parameter[] params = method.getParameters();
        Object[] args = new Object[params.length];

        for (int i = 0; i < params.length; i++) {
            Parameter param = params[i];
            Object value = arguments.get(param.getName());
            args[i] = convertValue(value, param.getType());
        }

        return args;
    }

    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;
        if (targetType.isInstance(value)) return value;

        String strValue = value.toString();
        if (targetType == String.class) return strValue;
        if (targetType == Integer.class || targetType == int.class) return Integer.parseInt(strValue);
        if (targetType == Long.class || targetType == long.class) return Long.parseLong(strValue);
        if (targetType == Double.class || targetType == double.class) return Double.parseDouble(strValue);
        if (targetType == Float.class || targetType == float.class) return Float.parseFloat(strValue);
        if (targetType == Boolean.class || targetType == boolean.class) return Boolean.parseBoolean(strValue);

        return value;
    }

    public boolean hasTool(String toolName) {
        return tools.containsKey(toolName);
    }

    public record ToolDefinition(
            String name,
            String description,
            Method method,
            Object instance,
            List<ToolParameter> parameters
    ) {}

    public record ToolParameter(
            String name,
            Class<?> type,
            String description,
            boolean required
    ) {}
}
