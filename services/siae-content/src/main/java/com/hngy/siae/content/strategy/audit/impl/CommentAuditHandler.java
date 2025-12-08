package com.hngy.siae.content.strategy.audit.impl;

import com.hngy.siae.content.entity.Comment;
import com.hngy.siae.content.enums.TypeEnum;
import com.hngy.siae.content.enums.status.CommentStatusEnum;
import com.hngy.siae.content.service.CommentsService;
import com.hngy.siae.content.strategy.audit.AuditHandler;
import com.hngy.siae.content.strategy.audit.AuditType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 评论审核处理器
 * 处理评论类型的审核逻辑
 * 
 * Requirements: 2.4, 2.5
 * 
 * @author Kiro
 */
@Slf4j
@AuditType(TypeEnum.COMMENT)
@Component
@RequiredArgsConstructor
public class CommentAuditHandler implements AuditHandler {

    private final CommentsService commentsService;

    /**
     * 审核通过处理
     * 更新评论状态为已通过
     * 
     * @param targetId 评论ID
     * @return 是否成功（乐观锁冲突时返回 false）
     */
    @Override
    public boolean onApproved(Long targetId) {
        // 使用乐观锁更新，返回是否成功
        return commentsService.updateStatus(targetId, CommentStatusEnum.PUBLISHED);
    }

    /**
     * 审核拒绝处理
     * 更新评论状态为已删除
     * 
     * @param targetId 评论ID
     * @param reason   拒绝原因
     * @return 是否成功（乐观锁冲突时返回 false）
     */
    @Override
    public boolean onRejected(Long targetId, String reason) {
        // 使用乐观锁更新，返回是否成功
        return commentsService.updateStatus(targetId, CommentStatusEnum.DELETED);
    }

    /**
     * 获取评论当前状态
     * 
     * @param targetId 评论ID
     * @return 当前状态码，如果评论不存在则返回 null
     */
    @Override
    public Integer getCurrentStatus(Long targetId) {
        Comment comment = commentsService.getById(targetId);
        return comment != null ? comment.getStatus().getCode() : null;
    }
}
