package com.xingchen.backend.service.impl;

import com.xingchen.backend.config.OrderWebSocketHandler;
import com.xingchen.backend.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * WebSocket推送服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService {

    private final OrderWebSocketHandler orderWebSocketHandler;

    @Override
    public void notifyUserOrderStatus(Long userId, Long orderId, Integer status, String message) {
        orderWebSocketHandler.sendOrderStatusToUser(userId, orderId, status, message);
    }

    @Override
    public void notifyMerchantNewOrder(Long merchantId, Long orderId, String message) {
        orderWebSocketHandler.sendNewOrderToMerchant(merchantId, orderId, message);
    }

    @Override
    public void notifyMerchantOrderStatus(Long merchantId, Long orderId, Integer status, String message) {
        orderWebSocketHandler.sendOrderStatusToMerchant(merchantId, orderId, status, message);
    }
}
