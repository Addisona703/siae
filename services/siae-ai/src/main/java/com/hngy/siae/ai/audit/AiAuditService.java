package com.hngy.siae.ai.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * AI审计服务
 * <p>
 * 记录AI交互的审计日志，包括用户身份、查询内容、时间戳等信息。
 * 用于安全审计和使用分析。
 * <p>
 * Requirements: 5.3, 7.4
 *
 * @author SIAE Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAuditService {

    /**
     * 记录AI交互
     *
     * @param userId      用户ID
     * @param username    用户名
     * @param sessionId   会话ID
     * @param queryContent 查询内容
     * @param timestamp   时间戳
     */
    public void logAiInteraction(Long userId, String username, String sessionId, 
                                 String queryContent, LocalDateTime timestamp) {
        log.info("AI_INTERACTION - userId: {}, username: {}, sessionId: {}, query: {}, timestamp: {}",
                userId, username, sessionId, queryContent, timestamp);
    }

    /**
     * 记录工具执行
     *
     * @param toolName        工具名称
     * @param parameters      工具参数
     * @param result          执行结果
     * @param executionTimeMs 执行时间（毫秒）
     * @param userId          用户ID
     * @param username        用户名
     */
    public void logToolExecution(String toolName, Map<String, Object> parameters, 
                                 Object result, Long executionTimeMs, 
                                 Long userId, String username) {
        log.info("TOOL_EXECUTION - toolName: {}, userId: {}, username: {}, " +
                "parameters: {}, executionTime: {}ms, resultType: {}",
                toolName, userId, username, parameters, executionTimeMs,
                result != null ? result.getClass().getSimpleName() : "null");
    }

    /**
     * 记录工具执行错误
     *
     * @param toolName        工具名称
     * @param parameters      工具参数
     * @param error           错误信息
     * @param executionTimeMs 执行时间（毫秒）
     * @param userId          用户ID
     * @param username        用户名
     */
    public void logToolExecutionError(String toolName, Map<String, Object> parameters,
                                      String error, Long executionTimeMs,
                                      Long userId, String username) {
        log.warn("TOOL_EXECUTION_ERROR - toolName: {}, userId: {}, username: {}, " +
                "parameters: {}, executionTime: {}ms, error: {}",
                toolName, userId, username, parameters, executionTimeMs, error);
    }
}
