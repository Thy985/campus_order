package com.xingchen.backend.service.impl;

import com.xingchen.backend.entity.Notification;
import com.xingchen.backend.service.NotificationService;
import com.xingchen.backend.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final UserNotificationService userNotificationService;

    @Override
    public List<Notification> getNotifications(Long userId, Integer isRead, int page, int size) {
        return userNotificationService.getUserNotifications(userId, isRead, page, size);
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return userNotificationService.getUnreadCount(userId);
    }

    @Override
    public void markAsRead(Long notificationId, Long userId) {
        userNotificationService.markAsRead(notificationId, userId);
    }

    @Override
    public void markAllAsRead(Long userId) {
        userNotificationService.markAllAsRead(userId);
    }
}
