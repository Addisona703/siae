package com.hngy.siae.content.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.ContentResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.content.dto.request.audit.AuditDTO;
import com.hngy.siae.content.enums.TypeEnum;
import com.hngy.siae.content.enums.status.AuditStatusEnum;
import com.hngy.siae.content.enums.status.CommentStatusEnum;
import com.hngy.siae.content.enums.status.ContentStatusEnum;
import com.hngy.siae.content.dto.request.comment.CommentCreateDTO;
import com.hngy.siae.content.dto.request.comment.CommentUpdateDTO;
import com.hngy.siae.content.dto.request.comment.CommentQueryDTO;
import com.hngy.siae.content.dto.response.comment.CommentVO;
import com.hngy.siae.content.entity.Comment;
import com.hngy.siae.content.entity.Content;
import com.hngy.siae.content.mapper.CommentMapper;
import com.hngy.siae.api.user.client.UserFeignClient;
import com.hngy.siae.api.user.dto.response.UserProfileSimpleVO;
import com.hngy.siae.content.service.AuditsService;
import com.hngy.siae.content.service.CommentsService;
import com.hngy.siae.content.service.ContentService;
import com.hngy.siae.messaging.event.MessagingConstants;
import com.hngy.siae.messaging.event.NotificationMessage;
import com.hngy.siae.messaging.producer.SiaeMessagingTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 内容评论服务impl
 *
 * @author KEYKB
 * 创建时间: 2025/05/21
 */
@Slf4j
@Service
public class CommentsServiceImpl
        extends ServiceImpl<CommentMapper, Comment>
        implements CommentsService {

    private final ContentService contentService;
    private final AuditsService auditsService;
    private final UserFeignClient userFeignClient;
    private final SiaeMessagingTemplate messagingTemplate;

    public CommentsServiceImpl(ContentService contentService,
                               @Lazy AuditsService auditsService,
                               UserFeignClient userFeignClient,
                               SiaeMessagingTemplate messagingTemplate) {
        this.contentService = contentService;
        this.auditsService = auditsService;
        this.userFeignClient = userFeignClient;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public Comment createComment(Long contentId, CommentCreateDTO commentCreateDTO) {
        // 检查内容状态，只有已发布的内容才能评论
        Content content = contentService.getById(contentId);
        AssertUtils.notNull(content, ContentResultCodeEnum.CONTENT_NOT_FOUND);
        AssertUtils.isTrue(content.getStatus() == ContentStatusEnum.PUBLISHED,
                ContentResultCodeEnum.CONTENT_NOT_PUBLISHED);

        Comment comment = BeanConvertUtil.to(commentCreateDTO, Comment.class);
        comment.setStatus(CommentStatusEnum.PUBLISHED);
        comment.setContentId(contentId);
        AssertUtils.isTrue(this.save(comment), ContentResultCodeEnum.COMMENT_CREATE_FAILED);
        return comment;
    }

    @Override
    public CommentVO updateComment(Long commentId, CommentUpdateDTO commentUpdateDTO) {
        Comment comment = this.getById(commentId);
        AssertUtils.notNull(comment, ContentResultCodeEnum.COMMENT_NOT_FOUND);

        // 从Security上下文获取当前用户信息
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        // 获取用户ID（从Details中获取，由ServiceAuthenticationFilter设置）
        Long currentUserId = (Long) authentication.getDetails();
        
        // 判断是否为管理员（检查是否有ROLE_ADMIN或ROLE_ROOT角色）
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ROLE_ROOT"));
        
        // 权限检查：非管理员只能更新自己的评论
        if (!isAdmin) {
            AssertUtils.isTrue(comment.getUserId().equals(currentUserId), 
                ContentResultCodeEnum.COMMENT_UPDATE_NO_PERMISSION);
        }

        comment.setContent(commentUpdateDTO.getContent());
        AssertUtils.isTrue(this.updateById(comment), ContentResultCodeEnum.COMMENT_UPDATE_FAILED);

        CommentVO commentVO = BeanConvertUtil.to(comment, CommentVO.class);
        
        // 填充用户信息
        enrichUserInfo(List.of(commentVO));
        
        return commentVO;
    }

    @Override
    public void deleteComment(Long id) {
        AssertUtils.notNull(this.getById(id), ContentResultCodeEnum.COMMENT_NOT_FOUND);
        AssertUtils.isTrue(this.removeById(id), ContentResultCodeEnum.COMMENT_DELETE_FAILED);
    }


    @Override
    public PageVO<CommentVO> listRootComments(Long contentId, PageDTO<CommentQueryDTO> pageDTO) {
        // 解析排序参数
        CommentQueryDTO params = pageDTO.getParams();
        String sortBy = params != null ? params.getSortBy() : null;
        String sortOrder = params != null ? params.getSortOrder() : null;

        // 分页查询根评论（XML中已包含子评论数量统计，不预加载子评论）
        Page<CommentVO> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        IPage<CommentVO> rootPage = baseMapper.selectRootCommentsPage(page, contentId, sortBy, sortOrder);
        List<CommentVO> rootComments = rootPage.getRecords();

        // 填充用户信息
        enrichUserInfo(rootComments);

        // 构建返回结果
        PageVO<CommentVO> result = new PageVO<>();
        result.setTotal(rootPage.getTotal());
        result.setPageNum((int) rootPage.getCurrent());
        result.setPageSize((int) rootPage.getSize());
        result.setRecords(rootComments);

        return result;
    }

    @Override
    public PageVO<CommentVO> listChildComments(Long contentId, Long rootId, PageDTO<Void> pageDTO) {
        // 分页查询子评论
        Page<CommentVO> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        IPage<CommentVO> childPage = baseMapper.selectChildCommentsPage(page, contentId, rootId);

        // 填充用户信息
        enrichUserInfo(childPage.getRecords());

        // 构建返回结果
        PageVO<CommentVO> result = new PageVO<>();
        result.setTotal(childPage.getTotal());
        result.setPageNum((int) childPage.getCurrent());
        result.setPageSize((int) childPage.getSize());
        result.setRecords(childPage.getRecords());

        return result;
    }

    /**
     * 填充评论的用户信息（昵称和头像，包括回复目标用户）
     *
     * @param commentVOs 评论VO列表
     */
    private void enrichUserInfo(List<CommentVO> commentVOs) {
        if (commentVOs == null || commentVOs.isEmpty()) {
            return;
        }

        // 收集所有用户ID（包括评论用户和回复目标用户）
        List<Long> userIds = commentVOs.stream()
                .flatMap(vo -> {
                    java.util.stream.Stream.Builder<Long> builder = java.util.stream.Stream.builder();
                    builder.add(vo.getUserId());
                    if (vo.getReplyToUserId() != null) {
                        builder.add(vo.getReplyToUserId());
                    }
                    return builder.build();
                })
                .distinct()
                .collect(Collectors.toList());

        // 批量查询用户信息
        Map<Long, UserProfileSimpleVO> userMap = userFeignClient.batchGetUserProfiles(userIds);

        // 填充用户信息到评论VO
        commentVOs.forEach(commentVO -> {
            // 填充评论用户信息
            UserProfileSimpleVO userProfile = userMap.get(commentVO.getUserId());
            if (userProfile != null) {
                commentVO.setUserNickname(userProfile.getNickname());
                commentVO.setUserAvatarFileId(userProfile.getAvatarFileId());
                commentVO.setUserAvatarUrl(userProfile.getAvatarUrl());
            }
            
            // 填充回复目标用户昵称
            if (commentVO.getReplyToUserId() != null) {
                UserProfileSimpleVO replyToUser = userMap.get(commentVO.getReplyToUserId());
                if (replyToUser != null) {
                    commentVO.setReplyToUserNickname(replyToUser.getNickname());
                }
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVO createCommentWithAudit(Long contentId, CommentCreateDTO commentCreateDTO) {
        // 验证内容是否存在
        Content content = contentService.getById(contentId);
        AssertUtils.notNull(content, ContentResultCodeEnum.CONTENT_NOT_FOUND);

        // 添加评论
        Comment comment = createComment(contentId, commentCreateDTO);

        // 添加审核记录，TODO: 后续添加机器审核评论
        AuditDTO auditDTO = AuditDTO.builder()
                .targetId(comment.getId())
                .targetType(TypeEnum.COMMENT)
                .auditStatus(AuditStatusEnum.APPROVED)
                .auditReason("自动审核")
                .auditBy(1L).build();
        auditsService.submitAudit(auditDTO);

        // TODO:内容的统计信息更新

        CommentVO commentVO = BeanConvertUtil.to(comment, CommentVO.class);
        
        // 填充用户信息
        enrichUserInfo(List.of(commentVO));

        // 发送评论通知
        sendCommentNotification(comment, content, commentCreateDTO.getUserId());
        
        return commentVO;
    }

    /**
     * 发送评论通知
     * 1. 如果是回复某人的评论，通知被回复者
     * 2. 如果是对内容的评论，通知内容作者（排除自己评论自己的情况）
     */
    private void sendCommentNotification(Comment comment, Content content, Long commentUserId) {
        try {
            // 获取评论者昵称
            String commenterNickname = "用户";
            Map<Long, UserProfileSimpleVO> userMap = userFeignClient.batchGetUserProfiles(List.of(commentUserId));
            if (userMap != null && userMap.containsKey(commentUserId)) {
                commenterNickname = userMap.get(commentUserId).getNickname();
            }

            // 情况1：回复某人的评论
            if (comment.getReplyToUserId() != null && !comment.getReplyToUserId().equals(commentUserId)) {
                NotificationMessage replyNotification = NotificationMessage.builder()
                        .userId(comment.getReplyToUserId())
                        .type(3) // REMIND 提醒类型
                        .title("收到新回复")
                        .content(commenterNickname + " 回复了你的评论")
                        .linkUrl("/content/" + content.getId())
                        .businessId(comment.getId())
                        .businessType("COMMENT_REPLY")
                        .build();

                messagingTemplate.send(
                        MessagingConstants.NOTIFICATION_EXCHANGE,
                        MessagingConstants.NOTIFICATION_COMMENT,
                        replyNotification
                );
                log.info("发送评论回复通知: toUserId={}, commentId={}", comment.getReplyToUserId(), comment.getId());
            }

            // 情况2：对内容的评论，通知内容作者（排除自己评论自己的内容）
            Long contentAuthorId = content.getUploadedBy();
            if (contentAuthorId != null && !contentAuthorId.equals(commentUserId)) {
                // 如果已经通知了被回复者，且被回复者就是内容作者，则不重复通知
                if (comment.getReplyToUserId() != null && comment.getReplyToUserId().equals(contentAuthorId)) {
                    return;
                }

                NotificationMessage contentNotification = NotificationMessage.builder()
                        .userId(contentAuthorId)
                        .type(3) // REMIND 提醒类型
                        .title("收到新评论")
                        .content(commenterNickname + " 评论了你的内容「" + truncateTitle(content.getTitle(), 20) + "」")
                        .linkUrl("/content/" + content.getId())
                        .businessId(comment.getId())
                        .businessType("CONTENT_COMMENT")
                        .build();

                messagingTemplate.send(
                        MessagingConstants.NOTIFICATION_EXCHANGE,
                        MessagingConstants.NOTIFICATION_COMMENT,
                        contentNotification
                );
                log.info("发送内容评论通知: toUserId={}, contentId={}", contentAuthorId, content.getId());
            }
        } catch (Exception e) {
            // 通知发送失败不影响主流程
            log.error("发送评论通知失败: commentId={}", comment.getId(), e);
        }
    }

    /**
     * 截断标题
     */
    private String truncateTitle(String title, int maxLength) {
        if (title == null) {
            return "";
        }
        if (title.length() <= maxLength) {
            return title;
        }
        return title.substring(0, maxLength) + "...";
    }

    @Override
    public void deleteCommentWithPermissionCheck(Long id) {
        Comment comment = this.getById(id);
        AssertUtils.notNull(comment, ContentResultCodeEnum.COMMENT_NOT_FOUND);

        // 从Security上下文获取当前用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
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
        deleteComment(id);

        // TODO:删除审核记录 + 统计信息更新
    }

    @Override
    public boolean updateStatus(Long commentId, CommentStatusEnum status) {
        Comment comment = this.getById(commentId);
        AssertUtils.notNull(comment, ContentResultCodeEnum.COMMENT_NOT_FOUND);
        comment.setStatus(status);
        // 使用乐观锁更新，version 字段会由 MyBatis-Plus 自动处理
        // 如果并发冲突，updateById 会返回 false
        return this.updateById(comment);
    }
}
