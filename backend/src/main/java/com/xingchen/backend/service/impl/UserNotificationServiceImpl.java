package com.xingchen.backend.service.impl;

import com.xingchen.backend.config.OrderWebSocketHandler;
import com.xingchen.backend.entity.Notification;
import com.xingchen.backend.mapper.NotificationMapper;
import com.xingchen.backend.service.UserNotificationService;
import com.xingchen.backend.util.constant.Constants;
import com.xingchen.backend.util.constant.ErrorCode;
import com.xingchen.backend.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserNotificationServiceImpl implements UserNotificationService {

    private final NotificationMapper notificationMapper;
    private final OrderWebSocketHandler orderWebSocketHandler;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Notification createSystemNotification(Long userId, String title, String content) {
        return createNotification(userId, Constants.NotificationType.SYSTEM, title, content, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Notification createOrderNotification(Long userId, Long orderId, String title, String content) {
        Map<String, Object> extraData = new HashMap<>();
        extraData.put("orderId", orderId);
        return createNotification(userId, Constants.NotificationType.ORDER, title, content, extraData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Notification createActivityNotification(Long userId, String title, String content, String extraData) {
        return createNotification(userId, Constants.NotificationType.ACTIVITY, title, content, extraData);
    }

    private Notification createNotification(Long userId, Integer type, String title, String content, Object extraData) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);

        if (extraData != null) {
            try {
                notification.setExtraData(objectMapper.writeValueAsString(extraData));
            } catch (Exception e) {
                log.error("序列化额外数据失败", e);
            }
        }

        notification.setIsRead(0);
        notification.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        notification.setCreateTime(LocalDateTime.now());

        notificationMapper.insert(notification);

        pushNotification(userId, notification);

        log.info("创建通知成功: notificationId={}, userId={}, type={}",
                notification.getId(), userId, type);

        return notification;
    }

    @Override
    public List<Notification> getUserNotifications(Long userId, Integer isRead, int page, int size) {
        // 先获取用户的所有通知，然后在内存中过滤和分页
        List<Notification> allNotifications = notificationMapper.selectByUserId(userId);

        // 过滤已读/未读
        if (isRead != null) {
            allNotifications = allNotifications.stream()
                    .filter(n -> n.getIsRead().equals(isRead))
                    .toList();
        }

        // 分页
        int offset = (page - 1) * size;
        int endIndex = Math.min(offset + size, allNotifications.size());

        if (offset >= allNotifications.size()) {
            return new java.util.ArrayList<>();
        }

        return allNotifications.subList(offset, endIndex);
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return (long) notificationMapper.countUnreadByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null || notification.getIsDeleted() == Constants.DeleteFlag.DELETED) {
            throw new BusinessException(ErrorCode.DATA_NOT_EXIST, "通知不存在");
        }

        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.USER_NO_PERMISSION, "无权操作此通知");
        }

        notification.setIsRead(1);
        notificationMapper.update(notification);

        log.info("标记通知已读: notificationId={}, userId={}", notificationId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
        log.info("标记所有通知已读: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null || notification.getIsDeleted() == Constants.DeleteFlag.DELETED) {
            throw new BusinessException(ErrorCode.DATA_NOT_EXIST, "通知不存在");
        }

        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.USER_NO_PERMISSION, "无权操作此通知");
        }

        notification.setIsDeleted(Constants.DeleteFlag.DELETED);
        notificationMapper.update(notification);

        log.info("删除通知: notificationId={}, userId={}", notificationId, userId);
    }

    @Override
    public void pushNotification(Long userId, Notification notification) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "NOTIFICATION");
            message.put("notificationId", notification.getId());
            message.put("notificationType", notification.getType());
            message.put("title", notification.getTitle());
            message.put("content", notification.getContent());
            message.put("createTime", notification.getCreateTime());

            orderWebSocketHandler.sendOrderStatusToUser(userId, null, null,
                    objectMapper.writeValueAsString(message));

            log.debug("推送通知成功: userId={}, notificationId={}", userId, notification.getId());
        } catch (Exception e) {
            log.error("推送通知失败: userId={}, notificationId={}", userId, notification.getId(), e);
        }
    }
}
