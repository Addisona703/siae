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
     * 工具名称
     */
    private String toolName;

    /**
     * 调用参数
     */
    private Map<String, Object> parameters;

    /**
     * 执行结果
     */
    private Object result;

    /**
     * 执行耗时（毫秒）
     */
    private Long executionTimeMs;
}
