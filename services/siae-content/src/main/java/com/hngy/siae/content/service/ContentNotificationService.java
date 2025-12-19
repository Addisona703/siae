package com.hngy.siae.content.service;

import com.hngy.siae.content.entity.Comment;
import com.hngy.siae.content.entity.Content;

/**
 * 内容通知服务接口
 * 负责封装所有内容相关的通知发送逻辑
 *
 * @author KEYKB
 */
public interface ContentNotificationService {

    /**
     * 发送内容审核通过通知
     *
     * @param content 审核通过的内容
     */
    void sendContentApprovedNotification(Content content);

    /**
     * 发送内容被点赞通知
     *
     * @param content    被点赞的内容
     * @param likeUserId 点赞用户ID
     */
    void sendContentLikeNotification(Content content, Long likeUserId);

    /**
     * 发送内容被收藏通知
     *
     * @param content        被收藏的内容
     * @param favoriteUserId 收藏用户ID
     */
    void sendContentFavoriteNotification(Content content, Long favoriteUserId);

    /**
     * 发送评论被点赞通知
     *
     * @param comment    被点赞的评论
     * @param likeUserId 点赞用户ID
     */
    void sendCommentLikeNotification(Comment comment, Long likeUserId);
}
