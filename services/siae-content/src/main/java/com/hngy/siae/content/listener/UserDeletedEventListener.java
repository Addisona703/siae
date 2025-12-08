package com.hngy.siae.content.listener;

import com.hngy.siae.messaging.annotation.DeclareQueue;
import com.hngy.siae.messaging.annotation.ExchangeType;
import com.hngy.siae.messaging.event.MessagingConstants;
import com.hngy.siae.messaging.event.UserDeletedEvent;
import com.hngy.siae.messaging.consumer.SiaeRabbitListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户删除事件监听器
 * <p>
 * 监听用户服务发送的用户删除事件，清理内容服务中的关联数据
 *
 * @author KEYKB
 */
@Slf4j
@Component
@RequiredArgsConstructor
@DeclareQueue(
        queue = MessagingConstants.CLEANUP_USER_CONTENT_QUEUE,
        exchange = MessagingConstants.CLEANUP_EXCHANGE,
        routingKey = MessagingConstants.CLEANUP_USER_DELETED,
        exchangeType = ExchangeType.TOPIC
)
public class UserDeletedEventListener {

    // TODO: 注入需要的 Service 或 Mapper
    // private final ContentMapper contentMapper;
    // private final CommentMapper commentMapper;

    /**
     * 处理用户删除事件
     * 清理该用户在内容服务中的相关数据
     */
    @SiaeRabbitListener(queues = MessagingConstants.CLEANUP_USER_CONTENT_QUEUE)
    public void handleUserDeletedEvent(UserDeletedEvent event) {
        log.info("收到用户删除事件: eventId={}, userIds={}", event.getEventId(), event.getUserIds());

        List<Long> userIds = event.getUserIds();
        if (userIds == null || userIds.isEmpty()) {
            log.warn("用户ID列表为空，跳过处理");
            return;
        }

        try {
            // 1. 清理用户发布的内容（可选：逻辑删除或物理删除）
            // int contentCount = contentMapper.deleteByUserIds(userIds);
            // log.info("删除用户内容: {} 条", contentCount);

            // 2. 清理用户的评论
            // int commentCount = commentMapper.deleteByUserIds(userIds);
            // log.info("删除用户评论: {} 条", commentCount);

            // 3. 清理用户的点赞记录
            // int likeCount = likeMapper.deleteByUserIds(userIds);
            // log.info("删除用户点赞: {} 条", likeCount);

            // 4. 清理用户的收藏记录
            // int favoriteCount = favoriteMapper.deleteByUserIds(userIds);
            // log.info("删除用户收藏: {} 条", favoriteCount);

            log.info("用户删除事件处理完成: eventId={}", event.getEventId());

        } catch (Exception e) {
            log.error("处理用户删除事件失败: eventId={}, userIds={}", event.getEventId(), userIds, e);
            // 抛出异常让消息重试
            throw new RuntimeException("处理用户删除事件失败", e);
        }
    }
}
