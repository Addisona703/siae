package com.hngy.siae.content.facade.impl;

import com.hngy.siae.content.entity.Content;
import com.hngy.siae.content.enums.TypeEnum;
import com.hngy.siae.content.enums.status.AuditStatusEnum;
import com.hngy.siae.content.dto.request.AuditDTO;
import com.hngy.siae.content.dto.request.CommentCreateDTO;
import com.hngy.siae.content.dto.response.CommentVO;
import com.hngy.siae.content.entity.Comment;
import com.hngy.siae.content.facade.CommentFacade;
import com.hngy.siae.content.service.AuditsService;
import com.hngy.siae.content.service.CommentsService;
import com.hngy.siae.content.service.ContentService;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.ContentResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public CommentVO createComment(Long contentId, CommentCreateDTO commentCreateDTO) {
        // 验证内容是否存在
        AssertUtils.notNull(contentService.getById(contentId), ContentResultCodeEnum.CONTENT_NOT_FOUND);

        // 添加评论
        Comment comment = commentsService.createComment(contentId, commentCreateDTO);

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

        return BeanConvertUtil.to(comment, CommentVO.class);
    }


    @Override
    public void deleteComment(Long id) {
        Comment comment = commentsService.getById(id);
        AssertUtils.notNull(comment, ContentResultCodeEnum.COMMENT_NOT_FOUND);

        // 从Security上下文获取当前用户信息
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        // 获取用户ID（从Details中获取，由ServiceAuthenticationFilter设置）
        Long currentUserId = (Long) authentication.getDetails();
        
        // 判断是否为管理员（检查是否有ROLE_ADMIN或ROLE_ROOT角色）
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ROLE_ROOT"));
        
        // 权限检查：管理员可以删除任何评论，评论作者可以删除自己的评论，内容创建者可以删除其内容下的评论
        if (!isAdmin) {
            boolean isCommentOwner = comment.getUserId().equals(currentUserId);
            boolean isContentOwner = false;
            
            // 检查是否是内容创建者
            if (!isCommentOwner) {
                Content content = contentService.getById(comment.getContentId());
                if (content != null) {
                    isContentOwner = content.getUploadedBy().equals(currentUserId);
                }
            }
            
            AssertUtils.isTrue(isCommentOwner || isContentOwner, 
                ContentResultCodeEnum.COMMENT_DELETE_NO_PERMISSION);
        }

        // 删除评论
        commentsService.deleteComment(id);

        // TODO:删除审核记录 + 统计信息更新
    }
}
