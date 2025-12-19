package com.hngy.siae.content.service.impl;

import com.hngy.siae.api.user.client.UserFeignClient;
import com.hngy.siae.api.user.dto.response.UserProfileSimpleVO;
import com.hngy.siae.content.constants.NotificationBusinessType;
import com.hngy.siae.content.entity.Comment;
import com.hngy.siae.content.entity.Content;
import com.hngy.siae.content.service.CommentsService;
import com.hngy.siae.content.service.ContentNotificationService;
import com.hngy.siae.content.service.ContentService;
import com.hngy.siae.messaging.event.MessagingConstants;
import com.hngy.siae.messaging.event.NotificationMessage;
import com.hngy.siae.messaging.producer.SiaeMessagingTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 内容通知服务实现类
 * 负责封装所有内容相关的通知发送逻辑
 *
 * @author KEYKB
 */
@Slf4j
@Service
public class ContentNotificationServiceImpl implements ContentNotificationService {

    private static final int NOTIFICATION_TYPE_REMIND = 3;
    private static final String DEFAULT_NICKNAME = "用户";
    private static final int MAX_TITLE_LENGTH = 20;
    private static final int MAX_COMMENT_LENGTH = 30;

    private final SiaeMessagingTemplate messagingTemplate;
    private final UserFeignClient userFeignClient;
    private final ContentService contentService;
    private final CommentsService commentsService;

    public ContentNotificationServiceImpl(
            SiaeMessagingTemplate messagingTemplate,
            UserFeignClient userFeignClient,
            @Lazy ContentService contentService,
            @Lazy CommentsService commentsService) {
        this.messagingTemplate = messagingTemplate;
        this.userFeignClient = userFeignClient;
        this.contentService = contentService;
        this.commentsService = commentsService;
    }


    @Override
    public void sendContentApprovedNotification(Content content) {
        if (content == null || content.getUploadedBy() == null) {
            log.warn("无法发送审核通过通知：内容或创建者ID为空");
            return;
        }

        try {
            NotificationMessage message = NotificationMessage.builder()
                    .userId(content.getUploadedBy())
                    .type(NOTIFICATION_TYPE_REMIND)
                    .title("内容审核通过")
                    .content("您的内容「" + truncateText(content.getTitle(), MAX_TITLE_LENGTH) + "」已通过审核")
                    .linkUrl("/content/detail/" + content.getId())
                    .businessId(content.getId())
                    .businessType(NotificationBusinessType.CONTENT_APPROVED)
                    .build();

            messagingTemplate.send(
                    MessagingConstants.NOTIFICATION_EXCHANGE,
                    MessagingConstants.NOTIFICATION_CONTENT,
                    message
            );
            log.info("发送内容审核通过通知成功: userId={}, contentId={}", content.getUploadedBy(), content.getId());
        } catch (Exception e) {
            log.error("发送内容审核通过通知失败: contentId={}", content.getId(), e);
        }
    }

    @Override
    public void sendContentLikeNotification(Content content, Long likeUserId) {
        if (content == null || content.getUploadedBy() == null || likeUserId == null) {
            log.warn("无法发送点赞通知：内容、创建者ID或点赞用户ID为空");
            return;
        }

        // 自己点赞自己的内容不发送通知
        if (content.getUploadedBy().equals(likeUserId)) {
            log.debug("用户点赞自己的内容，不发送通知: userId={}, contentId={}", likeUserId, content.getId());
            return;
        }

        try {
            String likerNickname = getUserNickname(likeUserId);

            NotificationMessage message = NotificationMessage.builder()
                    .userId(content.getUploadedBy())
                    .type(NOTIFICATION_TYPE_REMIND)
                    .title("收到新点赞")
                    .content(likerNickname + " 赞了你的内容「" + truncateText(content.getTitle(), MAX_TITLE_LENGTH) + "」")
                    .linkUrl("/content/detail/" + content.getId())
                    .businessId(content.getId())
                    .businessType(NotificationBusinessType.CONTENT_LIKE)
                    .build();

            messagingTemplate.send(
                    MessagingConstants.NOTIFICATION_EXCHANGE,
                    MessagingConstants.NOTIFICATION_CONTENT,
                    message
            );
            log.info("发送内容点赞通知成功: userId={}, contentId={}, likeUserId={}",
                    content.getUploadedBy(), content.getId(), likeUserId);
        } catch (Exception e) {
            log.error("发送内容点赞通知失败: contentId={}, likeUserId={}", content.getId(), likeUserId, e);
        }
    }

    @Override
    public void sendContentFavoriteNotification(Content content, Long favoriteUserId) {
        if (content == null || content.getUploadedBy() == null || favoriteUserId == null) {
            log.warn("无法发送收藏通知：内容、创建者ID或收藏用户ID为空");
            return;
        }

        // 自己收藏自己的内容不发送通知
        if (content.getUploadedBy().equals(favoriteUserId)) {
            log.debug("用户收藏自己的内容，不发送通知: userId={}, contentId={}", favoriteUserId, content.getId());
            return;
        }

        try {
            String favoriterNickname = getUserNickname(favoriteUserId);

            NotificationMessage message = NotificationMessage.builder()
                    .userId(content.getUploadedBy())
                    .type(NOTIFICATION_TYPE_REMIND)
                    .title("收到新收藏")
                    .content(favoriterNickname + " 收藏了你的内容「" + truncateText(content.getTitle(), MAX_TITLE_LENGTH) + "」")
                    .linkUrl("/content/detail/" + content.getId())
                    .businessId(content.getId())
                    .businessType(NotificationBusinessType.CONTENT_FAVORITE)
                    .build();

            messagingTemplate.send(
                    MessagingConstants.NOTIFICATION_EXCHANGE,
                    MessagingConstants.NOTIFICATION_CONTENT,
                    message
            );
            log.info("发送内容收藏通知成功: userId={}, contentId={}, favoriteUserId={}",
                    content.getUploadedBy(), content.getId(), favoriteUserId);
        } catch (Exception e) {
            log.error("发送内容收藏通知失败: contentId={}, favoriteUserId={}", content.getId(), favoriteUserId, e);
        }
    }


    @Override
    public void sendCommentLikeNotification(Comment comment, Long likeUserId) {
        if (comment == null || comment.getUserId() == null || likeUserId == null) {
            log.warn("无法发送评论点赞通知：评论、评论者ID或点赞用户ID为空");
            return;
        }

        // 自己点赞自己的评论不发送通知
        if (comment.getUserId().equals(likeUserId)) {
            log.debug("用户点赞自己的评论，不发送通知: userId={}, commentId={}", likeUserId, comment.getId());
            return;
        }

        try {
            String likerNickname = getUserNickname(likeUserId);

            NotificationMessage message = NotificationMessage.builder()
                    .userId(comment.getUserId())
                    .type(NOTIFICATION_TYPE_REMIND)
                    .title("评论收到点赞")
                    .content(likerNickname + " 赞了你的评论「" + truncateText(comment.getContent(), MAX_COMMENT_LENGTH) + "」")
                    .linkUrl("/content/detail/" + comment.getContentId())
                    .businessId(comment.getId())
                    .businessType(NotificationBusinessType.COMMENT_LIKE)
                    .build();

            messagingTemplate.send(
                    MessagingConstants.NOTIFICATION_EXCHANGE,
                    MessagingConstants.NOTIFICATION_COMMENT,
                    message
            );
            log.info("发送评论点赞通知成功: userId={}, commentId={}, likeUserId={}",
                    comment.getUserId(), comment.getId(), likeUserId);
        } catch (Exception e) {
            log.error("发送评论点赞通知失败: commentId={}, likeUserId={}", comment.getId(), likeUserId, e);
        }
    }

    /**
     * 获取用户昵称
     *
     * @param userId 用户ID
     * @return 用户昵称，获取失败时返回默认昵称
     */
    private String getUserNickname(Long userId) {
        try {
            Map<Long, UserProfileSimpleVO> userMap = userFeignClient.batchGetUserProfiles(List.of(userId));
            if (userMap != null && userMap.containsKey(userId)) {
                String nickname = userMap.get(userId).getNickname();
                if (nickname != null && !nickname.isEmpty()) {
                    return nickname;
                }
            }
        } catch (Exception e) {
            log.warn("获取用户昵称失败: userId={}", userId, e);
        }
        return DEFAULT_NICKNAME;
    }

    /**
     * 截断文本
     *
     * @param text      原始文本
     * @param maxLength 最大长度
     * @return 截断后的文本
     */
    private String truncateText(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
