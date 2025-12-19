package com.hngy.siae.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.notification.events.NotificationCreatedEvent;
import com.hngy.siae.core.utils.PageConvertUtil;
import com.hngy.siae.api.user.client.UserFeignClient;
import com.hngy.siae.notification.dto.request.NotificationBroadcastDTO;
import com.hngy.siae.notification.dto.request.NotificationCreateDTO;
import com.hngy.siae.notification.dto.response.NotificationVO;
import com.hngy.siae.notification.entity.SystemNotification;
import com.hngy.siae.notification.mapper.SystemNotificationMapper;
import com.hngy.siae.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 通知服务实现类
 *
 * @author KEYKB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<SystemNotificationMapper, SystemNotification> implements NotificationService {

    private final ApplicationEventPublisher publisher;
    private final UserFeignClient userFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendNotification(NotificationCreateDTO dto) {
        SystemNotification notification = BeanConvertUtil.to(dto, SystemNotification.class);
        notification.setIsRead(false);
        save(notification);
        log.info("发送通知成功 - 用户ID: {}, 标题: {}", dto.getUserId(), dto.getTitle());

        // 关键：发布事件，监听器会在事务提交后再推送
        publisher.publishEvent(new NotificationCreatedEvent(notification));
        return notification.getId();
    }

    @Override
    public PageVO<NotificationVO> getUserNotifications(Long userId, Integer page, Integer size, Boolean isRead) {
        LambdaQueryWrapper<SystemNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemNotification::getUserId, userId);

        if (isRead != null) {
            wrapper.eq(SystemNotification::getIsRead, isRead);
        }

        wrapper.orderByDesc(SystemNotification::getCreatedAt);

        Page<SystemNotification> pageResult = page(new Page<>(page, size), wrapper);
        return PageConvertUtil.convert(pageResult, NotificationVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long notificationId, Long userId) {
        LambdaUpdateWrapper<SystemNotification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SystemNotification::getId, notificationId)
               .eq(SystemNotification::getUserId, userId)
               .set(SystemNotification::getIsRead, true);

        boolean updated = update(wrapper);
        AssertUtils.isTrue(updated, "标记已读失败");
        log.info("标记通知已读 - 通知ID: {}, 用户ID: {}", notificationId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        LambdaUpdateWrapper<SystemNotification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SystemNotification::getUserId, userId)
               .eq(SystemNotification::getIsRead, false)
               .set(SystemNotification::getIsRead, true);

        update(wrapper);
        log.info("标记所有通知已读 - 用户ID: {}", userId);
    }

    @Override
    public Long getUnreadCount(Long userId) {
        LambdaQueryWrapper<SystemNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemNotification::getUserId, userId)
               .eq(SystemNotification::getIsRead, false);

        return count(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotification(Long notificationId, Long userId) {
        LambdaQueryWrapper<SystemNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemNotification::getId, notificationId)
               .eq(SystemNotification::getUserId, userId);

        boolean deleted = remove(wrapper);
        AssertUtils.isTrue(deleted, "删除通知失败");
        log.info("删除通知 - 通知ID: {}, 用户ID: {}", notificationId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteReadNotifications(Long userId) {
        LambdaQueryWrapper<SystemNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemNotification::getUserId, userId)
               .eq(SystemNotification::getIsRead, true);

        int deletedCount = getBaseMapper().delete(wrapper);
        log.info("批量删除已读通知 - 用户ID: {}, 删除数量: {}", userId, deletedCount);
        return deletedCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReadStatus(Long notificationId, Long userId, Boolean isRead) {
        LambdaUpdateWrapper<SystemNotification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SystemNotification::getId, notificationId)
               .eq(SystemNotification::getUserId, userId)
               .set(SystemNotification::getIsRead, isRead);

        boolean updated = update(wrapper);
        AssertUtils.isTrue(updated, "更新通知状态失败");
        log.info("更新通知状态 - 通知ID: {}, 用户ID: {}, 已读: {}", notificationId, userId, isRead);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAllReadStatus(Long userId, Boolean isRead) {
        LambdaUpdateWrapper<SystemNotification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SystemNotification::getUserId, userId)
               .set(SystemNotification::getIsRead, isRead);

        update(wrapper);
        log.info("更新所有通知状态 - 用户ID: {}, 已读: {}", userId, isRead);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int broadcastNotification(NotificationBroadcastDTO dto) {
        List<Long> targetUserIds;
        
        // 如果指定了用户ID列表，则发送给指定用户；否则发送给所有用户
        if (dto.getUserIds() != null && !dto.getUserIds().isEmpty()) {
            targetUserIds = dto.getUserIds();
            log.info("广播通知给指定用户 - 用户数量: {}", targetUserIds.size());
        } else {
            // 获取所有用户ID
            targetUserIds = userFeignClient.getAllUserIds();
            if (targetUserIds == null || targetUserIds.isEmpty()) {
                log.warn("没有找到任何用户，广播通知取消");
                return 0;
            }
            log.info("广播通知给所有用户 - 用户数量: {}", targetUserIds.size());
        }
        
        int successCount = 0;
        for (Long userId : targetUserIds) {
            try {
                SystemNotification notification = new SystemNotification();
                notification.setUserId(userId);
                notification.setType(dto.getType());
                notification.setTitle(dto.getTitle());
                notification.setContent(dto.getContent());
                notification.setLinkUrl(dto.getLinkUrl());
                notification.setIsRead(false);
                save(notification);
                
                // 发布事件进行实时推送
                publisher.publishEvent(new NotificationCreatedEvent(notification));
                successCount++;
            } catch (Exception e) {
                log.error("发送通知给用户 {} 失败: {}", userId, e.getMessage());
            }
        }
        
        log.info("广播通知完成 - 成功发送: {}/{}", successCount, targetUserIds.size());
        return successCount;
    }
}
