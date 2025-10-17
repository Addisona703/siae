package com.hngy.siae.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.web.utils.PageConvertUtil;
import com.hngy.siae.notification.dto.request.NotificationCreateDTO;
import com.hngy.siae.notification.dto.response.NotificationVO;
import com.hngy.siae.notification.entity.SystemNotification;
import com.hngy.siae.notification.mapper.SystemNotificationMapper;
import com.hngy.siae.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 通知服务实现类
 *
 * @author KEYKB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<SystemNotificationMapper, SystemNotification> implements NotificationService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendNotification(NotificationCreateDTO dto) {
        SystemNotification notification = BeanConvertUtil.to(dto, SystemNotification.class);
        notification.setIsRead(false);
        save(notification);
        log.info("发送通知成功 - 用户ID: {}, 标题: {}", dto.getUserId(), dto.getTitle());
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
}