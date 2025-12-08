package com.hngy.siae.media.listener;

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
 * 监听用户服务发送的用户删除事件，清理媒体服务中的关联数据
 *
 * @author KEYKB
 */
@Slf4j
@Component
@RequiredArgsConstructor
@DeclareQueue(
        queue = MessagingConstants.CLEANUP_USER_MEDIA_QUEUE,
        exchange = MessagingConstants.CLEANUP_EXCHANGE,
        routingKey = MessagingConstants.CLEANUP_USER_DELETED,
        exchangeType = ExchangeType.TOPIC
)
public class UserDeletedEventListener {

    // TODO: 注入需要的 Service 或 Mapper
    // private final MediaFileMapper mediaFileMapper;
    // private final MinioService minioService;

    /**
     * 处理用户删除事件
     * 清理该用户在媒体服务中的相关数据
     */
    @SiaeRabbitListener(queues = MessagingConstants.CLEANUP_USER_MEDIA_QUEUE)
    public void handleUserDeletedEvent(UserDeletedEvent event) {
        log.info("收到用户删除事件: eventId={}, userIds={}", event.getEventId(), event.getUserIds());

        List<Long> userIds = event.getUserIds();
        if (userIds == null || userIds.isEmpty()) {
            log.warn("用户ID列表为空，跳过处理");
            return;
        }

        try {
            // 1. 查询用户上传的文件列表
            // List<MediaFile> files = mediaFileMapper.selectByUserIds(userIds);

            // 2. 从对象存储中删除文件
            // for (MediaFile file : files) {
            //     minioService.deleteFile(file.getBucket(), file.getObjectKey());
            // }
            // log.info("删除对象存储文件: {} 个", files.size());

            // 3. 删除文件元数据记录
            // int fileCount = mediaFileMapper.deleteByUserIds(userIds);
            // log.info("删除文件记录: {} 条", fileCount);

            log.info("用户删除事件处理完成: eventId={}", event.getEventId());

        } catch (Exception e) {
            log.error("处理用户删除事件失败: eventId={}, userIds={}", event.getEventId(), userIds, e);
            throw new RuntimeException("处理用户删除事件失败", e);
        }
    }
}
