package com.xingchen.backend.service;

import com.xingchen.backend.entity.Notification;

import java.util.List;

public interface NotificationService {

    List<Notification> getNotifications(Long userId, Integer isRead, int page, int size);

    Long getUnreadCount(Long userId);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);
}
