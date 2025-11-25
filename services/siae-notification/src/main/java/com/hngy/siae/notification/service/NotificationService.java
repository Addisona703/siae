package com.hngy.siae.notification.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.notification.dto.request.NotificationCreateDTO;
import com.hngy.siae.notification.dto.response.NotificationVO;
import com.hngy.siae.notification.entity.SystemNotification;

/**
 * 通知服务接口
 *
 * @author KEYKB
 */
public interface NotificationService extends IService<SystemNotification> {

    /**
     * 发送系统通知
     */
    Long sendNotification(NotificationCreateDTO dto);

    /**
     * 获取用户通知列表
     */
    PageVO<NotificationVO> getUserNotifications(Long userId, Integer page, Integer size, Boolean isRead);

    /**
     * 标记通知为已读
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * 标记所有通知为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 更新通知已读状态
     */
    void updateReadStatus(Long notificationId, Long userId, Boolean isRead);

    /**
     * 更新所有通知已读状态
     */
    void updateAllReadStatus(Long userId, Boolean isRead);

    /**
     * 获取未读通知数量
     */
    Long getUnreadCount(Long userId);

    /**
     * 删除通知
     */
    void deleteNotification(Long notificationId, Long userId);
}