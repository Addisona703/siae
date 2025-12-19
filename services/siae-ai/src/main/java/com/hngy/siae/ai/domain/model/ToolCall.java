package com.hngy.siae.ai.domain.model;

/**
 * 工具调用记录
 * 
 * @param id        工具调用ID
 * @param name      工具名称
 * @param arguments 调用参数（JSON字符串）
 */
public record ToolCall(String id, String name, String arguments) {
}
