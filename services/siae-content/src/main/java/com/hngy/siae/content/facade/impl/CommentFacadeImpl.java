package com.hngy.siae.content.facade.impl;

import cn.hutool.core.bean.BeanUtil;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.content.common.enums.TypeEnum;
import com.hngy.siae.content.common.enums.status.AuditStatusEnum;
import com.hngy.siae.content.dto.request.AuditDTO;
import com.hngy.siae.content.dto.request.CommentDTO;
import com.hngy.siae.content.dto.response.CommentVO;
import com.hngy.siae.content.entity.Comment;
import com.hngy.siae.content.facade.CommentFacade;
import com.hngy.siae.content.service.AuditsService;
import com.hngy.siae.content.service.CommentsService;
import com.hngy.siae.content.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hngy.siae.core.asserts.AssertUtils;

/**
 * 评论外观impl
 *
 * @author KEYKB
 * 创建时间: 2025/05/21
 */
@Service
@RequiredArgsConstructor
public class CommentFacadeImpl implements CommentFacade {

    private final ContentService contentService;
    private final CommentsService commentsService;
    private final AuditsService auditsService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<CommentVO> createComment(Long contentId, CommentDTO commentDTO) {
        // 验证内容是否存在
        AssertUtils.notNull(contentService.getById(contentId), "内容不存在");

        // 添加评论
        Comment comment = commentsService.createComment(contentId, commentDTO);

        // 添加审核记录，TODO: 后续添加机器审核评论
        AuditDTO auditDTO = AuditDTO.builder()
                .targetId(comment.getId())
                .targetType(TypeEnum.COMMENT)
                .auditStatus(AuditStatusEnum.APPROVED)
                .auditReason("自动审核")
                .auditBy(1L).build();
        Long id = auditsService.submitAudit(auditDTO);
        // TODO:auditsService.handleAudit(id, auditDTO);后续需要处理评论审核问题

        // TODO:内容的统计信息更新

        CommentVO vo = BeanUtil.copyProperties(comment, CommentVO.class);
        return Result.success(vo);
    }


    @Override
    public Result<Void> deleteComment(Long id) {
        // 删除评论
        commentsService.deleteComment(id);

        // TODO:删除审核记录 + 统计信息更新
        return null;
    }
}
