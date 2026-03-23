package com.xingchen.backend.service;

import com.xingchen.backend.entity.Notification;

import java.util.List;

/**
 * 用户站内消息服务接口
 */
public interface UserNotificationService {

    /**
     * 创建系统通知
     */
    Notification createSystemNotification(Long userId, String title, String content);

    /**
     * 创建订单通知
     */
    Notification createOrderNotification(Long userId, Long orderId, String title, String content);

    /**
     * 创建活动通知
     */
    Notification createActivityNotification(Long userId, String title, String content, String extraData);

    /**
     * 获取用户的消息列表
     */
    List<Notification> getUserNotifications(Long userId, Integer isRead, int page, int size);

    /**
     * 获取未读消息数量
     */
    Long getUnreadCount(Long userId);

    /**
     * 标记消息为已读
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * 标记所有消息为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 删除消息
     */
    void deleteNotification(Long notificationId, Long userId);

    /**
     * 推送通知（WebSocket）
     */
    void pushNotification(Long userId, Notification notification);
}
