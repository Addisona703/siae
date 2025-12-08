package com.hngy.siae.content.strategy.audit;

/**
 * 审核处理器接口
 * 定义不同类型审核的处理逻辑
 * 使用 @AuditType 注解标记处理器支持的类型
 * <p>
 * Requirements: 2.1
 * 
 * @author Kiro
 */
public interface AuditHandler {

    /**
     * 审核通过处理
     * 
     * @param targetId 目标ID
     * @return 是否成功（乐观锁冲突时返回 false）
     */
    boolean onApproved(Long targetId);

    /**
     * 审核拒绝处理
     * 
     * @param targetId 目标ID
     * @param reason   拒绝原因
     * @return 是否成功（乐观锁冲突时返回 false）
     */
    boolean onRejected(Long targetId, String reason);

    /**
     * 获取目标当前状态
     * 
     * @param targetId 目标ID
     * @return 当前状态码，如果目标不存在则返回 null
     */
    Integer getCurrentStatus(Long targetId);
}
