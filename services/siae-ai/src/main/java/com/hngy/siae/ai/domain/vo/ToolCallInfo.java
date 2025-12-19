package com.hngy.siae.ai.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 工具调用信息VO
 * <p>
 * 记录AI调用工具函数的详细信息
 * <p>
 * Requirements: 5.1, 5.3
 *
 * @author SIAE Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCallInfo {

    /**
     * 工具调用ID（用于关联工具调用和结果）
     */
    private String id;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 调用参数（JSON字符串或Map）
     */
    private Map<String, Object> parameters;

    /**
     * 调用参数（原始JSON字符串）
     */
    private String argumentsJson;

    /**
     * 执行结果
     */
    private Object result;

    /**
     * 执行耗时（毫秒）
     */
    private Long executionTimeMs;

    /**
     * 是否执行成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String error;
}
