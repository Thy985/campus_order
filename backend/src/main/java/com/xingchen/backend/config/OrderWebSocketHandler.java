package com.xingchen.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class OrderWebSocketHandler extends TextWebSocketHandler {

    private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private static final Map<Long, WebSocketSession> merchantSessions = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        Long merchantId = getMerchantIdFromSession(session);

        if (userId != null) {
            userSessions.put(userId, session);
            log.info("User WebSocket connected: userId={}", userId);
        }

        if (merchantId != null) {
            merchantSessions.put(merchantId, session);
            log.info("Merchant WebSocket connected: merchantId={}", merchantId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        Long merchantId = getMerchantIdFromSession(session);

        if (userId != null) {
            userSessions.remove(userId);
            log.info("User WebSocket disconnected: userId={}", userId);
        }

        if (merchantId != null) {
            merchantSessions.remove(merchantId);
            log.info("Merchant WebSocket disconnected: merchantId={}", merchantId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("Received WebSocket message: {}", payload);

        if ("ping".equals(payload)) {
            session.sendMessage(new TextMessage("pong"));
        }
    }

    public void sendOrderStatusToUser(Long userId, Long orderId, Integer status, String message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                OrderStatusMessage msg = new OrderStatusMessage();
                msg.setType("ORDER_STATUS_CHANGE");
                msg.setOrderId(orderId);
                msg.setStatus(status);
                msg.setMessage(message);

                String json = objectMapper.writeValueAsString(msg);
                session.sendMessage(new TextMessage(json));
                log.info("Order status sent to user: userId={}, orderId={}, status={}", userId, orderId, status);
            } catch (IOException e) {
                log.error("Failed to send order status: userId={}, orderId={}", userId, orderId, e);
            }
        }
    }

    public void sendNewOrderToMerchant(Long merchantId, Long orderId, String message) {
        WebSocketSession session = merchantSessions.get(merchantId);
        if (session != null && session.isOpen()) {
            try {
                OrderStatusMessage msg = new OrderStatusMessage();
                msg.setType("NEW_ORDER");
                msg.setOrderId(orderId);
                msg.setMessage(message);

                String json = objectMapper.writeValueAsString(msg);
                session.sendMessage(new TextMessage(json));
                log.info("New order sent to merchant: merchantId={}, orderId={}", merchantId, orderId);
            } catch (IOException e) {
                log.error("Failed to send new order: merchantId={}, orderId={}", merchantId, orderId, e);
            }
        }
    }

    public void sendOrderStatusToMerchant(Long merchantId, Long orderId, Integer status, String message) {
        WebSocketSession session = merchantSessions.get(merchantId);
        if (session != null && session.isOpen()) {
            try {
                OrderStatusMessage msg = new OrderStatusMessage();
                msg.setType("ORDER_STATUS_CHANGE");
                msg.setOrderId(orderId);
                msg.setStatus(status);
                msg.setMessage(message);

                String json = objectMapper.writeValueAsString(msg);
                session.sendMessage(new TextMessage(json));
                log.info("Order status sent to merchant: merchantId={}, orderId={}, status={}", merchantId, orderId, status);
            } catch (IOException e) {
                log.error("Failed to send order status: merchantId={}, orderId={}", merchantId, orderId, e);
            }
        }
    }

    private Long getUserIdFromSession(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }

    private Long getMerchantIdFromSession(WebSocketSession session) {
        Object merchantId = session.getAttributes().get("merchantId");
        return merchantId != null ? Long.valueOf(merchantId.toString()) : null;
    }

    public static class OrderStatusMessage {
        private String type;
        private Long orderId;
        private Integer status;
        private String message;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
