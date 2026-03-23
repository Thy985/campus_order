package com.xingchen.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.xingchen.backend.entity.Notification;
import com.xingchen.backend.service.NotificationService;
import com.xingchen.backend.service.UserNotificationService;
import com.xingchen.backend.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息通知控制器
 *
 * @author xingchen
 * @date 2026-02-15
 */
@Slf4j
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
@Tag(name = "消息通知", description = "消息通知相关接口")
public class NotificationController {

    private final UserNotificationService userNotificationService;
    private final NotificationService notificationService;

    /**
     * 获取用户的消息列表
     */
    @SaCheckLogin
    @GetMapping
    @Operation(summary = "获取消息列表", description = "获取当前用户的消息通知列表")
    public Result<List<Notification>> getNotifications(
            @Parameter(description = "是否已读：0-未读，1-已读，不传则全部") @RequestParam(required = false) Integer isRead,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int size) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Notification> notifications = notificationService.getNotifications(userId, isRead, page, size);
        return Result.success("查询成功", notifications);
    }

    /**
     * 获取未读消息数量
     */
    @SaCheckLogin
    @GetMapping("/unread-count")
    @Operation(summary = "获取未读数量", description = "获取当前用户未读消息数量")
    public Result<Map<String, Long>> getUnreadCount() {
        Long userId = StpUtil.getLoginIdAsLong();
        Long count = notificationService.getUnreadCount(userId);
        Map<String, Long> result = new HashMap<>();
        result.put("unreadCount", count);
        return Result.success("查询成功", result);
    }

    /**
     * 标记消息为已读
     */
    @SaCheckLogin
    @PutMapping("/{notificationId}/read")
    @Operation(summary = "标记已读", description = "将指定消息标记为已读")
    public Result<Void> markAsRead(
            @Parameter(description = "消息ID") @PathVariable Long notificationId) {
        Long userId = StpUtil.getLoginIdAsLong();
        notificationService.markAsRead(notificationId, userId);
        return Result.success("标记成功", null);
    }

    /**
     * 标记所有消息为已读
     */
    @SaCheckLogin
    @PutMapping("/read-all")
    @Operation(summary = "全部已读", description = "将所有消息标记为已读")
    public Result<Void> markAllAsRead() {
        Long userId = StpUtil.getLoginIdAsLong();
        notificationService.markAllAsRead(userId);
        return Result.success("标记成功", null);
    }

    /**
     * 删除消息
     */
    @SaCheckLogin
    @DeleteMapping("/delete/{notificationId}")
    @Operation(summary = "删除消息", description = "删除指定消息")
    public Result<Void> deleteNotification(
            @Parameter(description = "消息ID") @PathVariable Long notificationId) {
        Long userId = StpUtil.getLoginIdAsLong();
        userNotificationService.deleteNotification(notificationId, userId);
        return Result.success("删除成功", null);
    }
}
