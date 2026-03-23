package com.xingchen.backend.service.impl;

import com.xingchen.backend.entity.Order;
import com.xingchen.backend.mapper.OrderMapper;
import com.xingchen.backend.service.OrderService;
import com.xingchen.backend.service.OrderTimeoutService;
import com.xingchen.backend.service.WebSocketService;
import com.xingchen.backend.util.constant.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单超时处理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderTimeoutServiceImpl implements OrderTimeoutService {

    private final OrderMapper orderMapper;
    private final OrderService orderService;
    private final WebSocketService webSocketService;

    // 订单超时时间（分钟），从配置读取，默认15分钟
    @Value("${order.timeout.minutes:15}")
    private int orderTimeoutMinutes;

    @Override
    @Scheduled(fixedRate = 60000)
    @Transactional(rollbackFor = Exception.class)
    public void scanTimeoutOrders() {
        log.debug("开始扫描超时订单...");

        // 查询待支付且超时的订单
        List<Order> timeoutOrders = orderMapper.selectTimeoutOrders(orderTimeoutMinutes);

        if (timeoutOrders.isEmpty()) {
            log.debug("没有超时订单");
            return;
        }

        log.info("发现 {} 个超时订单", timeoutOrders.size());

        // 处理每个超时订单
        for (Order order : timeoutOrders) {
            try {
                handleTimeoutOrder(order);
            } catch (Exception e) {
                log.error("处理超时订单失败: orderId={}, orderNo={}",
                        order.getId(), order.getOrderNo(), e);
            }
        }
    }

    /**
     * 处理单个超时订单
     */
    private void handleTimeoutOrder(Order order) {
        log.info("处理超时订单: orderId={}, orderNo={}, createTime={}",
                order.getId(), order.getOrderNo(), order.getCreateTime());

        // 回滚库存
        orderService.rollbackStock(order.getId());

        // 更新订单状态为已取消
        order.setStatus(Constants.OrderStatus.CANCELLED);
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason("订单超时未支付，系统自动取消");
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.update(order);

        // WebSocket通知用户订单已取消
        webSocketService.notifyUserOrderStatus(
                order.getUserId(),
                order.getId(),
                Constants.OrderStatus.CANCELLED,
                "订单超时未支付，已自动取消"
        );

        log.info("超时订单处理完成: orderId={}, orderNo={}",
                order.getId(), order.getOrderNo());
    }

    @Override
    public void manualCheckTimeout() {
        log.info("手动触发超时订单检查");
        scanTimeoutOrders();
    }
}
