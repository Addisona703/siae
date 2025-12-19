package com.hngy.siae.ai.client;

import com.hngy.siae.ai.domain.model.ToolCall;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工具调用聚合器
 * 用于在流式响应中聚合工具调用的增量数据
 */
@Slf4j
public class ToolCallAggregator {

    private final Map<Integer, ToolCallBuilder> builders = new HashMap<>();

    /**
     * 添加工具调用增量
     */
    public void addDelta(StreamResponseParser.ToolCallDelta delta) {
        ToolCallBuilder builder = builders.computeIfAbsent(delta.index(), k -> new ToolCallBuilder());
        builder.addDelta(delta);
    }

    /**
     * 添加多个工具调用增量
     */
    public void addDeltas(List<StreamResponseParser.ToolCallDelta> deltas) {
        if (deltas != null) {
            for (var delta : deltas) {
                addDelta(delta);
            }
        }
    }

    /**
     * 获取已完成的工具调用列表
     */
    public List<ToolCall> getToolCalls() {
        List<ToolCall> result = new ArrayList<>();
        for (ToolCallBuilder builder : builders.values()) {
            ToolCall toolCall = builder.build();
            if (toolCall != null) {
                result.add(toolCall);
            }
        }
        return result;
    }

    /**
     * 检查是否有工具调用
     */
    public boolean hasToolCalls() {
        return !builders.isEmpty();
    }

    /**
     * 重置聚合器
     */
    public void reset() {
        builders.clear();
    }

    /**
     * 工具调用构建器
     */
    private static class ToolCallBuilder {
        private String id;
        private String name;
        private final StringBuilder arguments = new StringBuilder();

        void addDelta(StreamResponseParser.ToolCallDelta delta) {
            if (delta.id() != null && !delta.id().isEmpty()) {
                this.id = delta.id();
            }
            if (delta.name() != null && !delta.name().isEmpty()) {
                this.name = delta.name();
            }
            if (delta.argumentsDelta() != null) {
                this.arguments.append(delta.argumentsDelta());
            }
        }

        ToolCall build() {
            if (id == null || name == null) {
                return null;
            }
            return new ToolCall(id, name, arguments.toString());
        }
    }
}
