package com.hngy.siae.content.strategy.audit.impl;

import com.hngy.siae.content.entity.Content;
import com.hngy.siae.content.enums.TypeEnum;
import com.hngy.siae.content.enums.status.ContentStatusEnum;
import com.hngy.siae.content.service.ContentService;
import com.hngy.siae.content.service.StatisticsService;
import com.hngy.siae.content.strategy.audit.AuditHandler;
import com.hngy.siae.content.strategy.audit.AuditType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 内容审核处理器
 * 处理内容类型的审核逻辑
 * 
 * Requirements: 2.2, 2.3
 * 
 * @author Kiro
 */
@Slf4j
@AuditType(TypeEnum.CONTENT)
@Component
@RequiredArgsConstructor
public class ContentAuditHandler implements AuditHandler {

    private final ContentService contentService;
    private final StatisticsService statisticsService;

    /**
     * 审核通过处理
     * 1. 更新内容状态为已发布（使用乐观锁）
     * 2. 创建统计记录
     * 
     * @param targetId 内容ID
     * @return 是否成功（乐观锁冲突时返回 false）
     */
    @Override
    public boolean onApproved(Long targetId) {
        // 1. 更新内容状态为已发布（使用乐观锁，返回是否成功）
        boolean success = contentService.updateStatus(targetId, ContentStatusEnum.PUBLISHED);
        if (success) {
            // 2. 创建统计记录
            try {
                statisticsService.addContentStatistics(targetId);
            } catch (Exception e) {
                log.warn("创建内容统计记录失败，contentId: {}", targetId, e);
                // 统计记录创建失败不影响审核结果
            }
        }
        return success;
    }

    /**
     * 审核拒绝处理
     * 更新内容状态为草稿
     * 
     * @param targetId 内容ID
     * @param reason   拒绝原因
     * @return 是否成功（乐观锁冲突时返回 false）
     */
    @Override
    public boolean onRejected(Long targetId, String reason) {
        // 更新内容状态为草稿（使用乐观锁，返回是否成功）
        return contentService.updateStatus(targetId, ContentStatusEnum.DRAFT);
    }

    /**
     * 获取内容当前状态
     * 
     * @param targetId 内容ID
     * @return 当前状态码，如果内容不存在则返回 null
     */
    @Override
    public Integer getCurrentStatus(Long targetId) {
        Content content = contentService.getById(targetId);
        return content != null ? content.getStatus().getCode() : null;
    }
}
