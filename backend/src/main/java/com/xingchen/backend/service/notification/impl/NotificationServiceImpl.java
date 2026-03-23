package com.xingchen.backend.service.notification.impl;

import com.xingchen.backend.entity.Order;
import com.xingchen.backend.entity.User;
import com.xingchen.backend.mapper.UserMapper;
import com.xingchen.backend.service.notification.NotificationService;
import com.xingchen.backend.service.sms.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 通知服务实现
 * 
 * 支持多种通知方式:
 * 1. 短信通知(已集成)
 * 2. 邮件通知(待实现)
 * 3. 微信模板消息(待实现)
 * 4. APP推送(待实现)
 * 5. 站内消息(待实现)
 * 
 * @author 小跃
 * @date 2026-02-14
 */
@Slf4j
@Service("smsNotificationService")
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    
    private final SmsService smsService;
    private final UserMapper userMapper;
    
    @Override
    public void sendOrderTimeoutNotification(Order order) {
        log.info("========== 发送订单超时取消通知 ==========");
        log.info("订单号: {}", order.getOrderNo());
        log.info("用户ID: {}", order.getUserId());
        log.info("订单金额: {}", order.getTotalAmount());
        
        try {
            // 获取用户信息
            User user = userMapper.selectOneById(order.getUserId());
            if (user == null || user.getPhone() == null) {
                log.warn("用户不存在或手机号为空，无法发送短信，userId={}", order.getUserId());
                return;
            }
            
            // 发送短信通知
            boolean success = smsService.sendOrderTimeoutNotification(
                user.getPhone(), 
                order.getOrderNo()
            );
            
            if (success) {
                log.info("订单超时取消通知发送成功，phone={}", maskPhone(user.getPhone()));
            } else {
                log.error("订单超时取消通知发送失败，phone={}", maskPhone(user.getPhone()));
            }
            
            // TODO: 可以在此处添加其他通知方式
            // emailService.sendOrderTimeoutEmail(user.getEmail(), order);
            // pushService.sendOrderTimeoutPush(user.getId(), order);
            
        } catch (Exception e) {
            log.error("发送订单超时取消通知异常: orderNo={}", order.getOrderNo(), e);
        }
        
        log.info("==========================================");
    }
    
    @Override
    public void sendPaymentSuccessNotification(Order order) {
        log.info("========== 发送支付成功通知 ==========");
        log.info("订单? {}", order.getOrderNo());
        log.info("订单金额: {}", order.getTotalAmount());
        
        try {
            // 1. 通知用户
            User user = userMapper.selectOneById(order.getUserId());
            if (user != null && user.getPhone() != null) {
                String amount = order.getTotalAmount().toString();
                boolean success = smsService.sendPaymentSuccessNotification(
                    user.getPhone(), 
                    order.getOrderNo(),
                    amount
                );
                
                if (success) {
                    log.info("[通知用户] 支付成功通知发送成功，phone={}", maskPhone(user.getPhone()));
                } else {
                    log.error("[通知用户] 支付成功通知发送失败，phone={}", maskPhone(user.getPhone()));
                }
            }
            
            // 2. 通知商家
            // 注意: 需要根据order.getMerchantId()获取商家信息
            // Merchant merchant = merchantMapper.selectOneById(order.getMerchantId());
            // if (merchant != null && merchant.getPhone() != null) {
            //     String amount = order.getTotalAmount().toString();
            //     boolean success = smsService.sendNewOrderNotification(
            //         merchant.getPhone(),
            //         order.getOrderNo(),
            //         amount
            //     );
            //     
            //     if (success) {
            //         log.info("[通知商家] 新订单通知发送成? phone={}", maskPhone(merchant.getPhone()));
            //     }
            // }
            
            log.info("[通知商家] 收到新订单: {}", order.getOrderNo());
            
        } catch (Exception e) {
            log.error("发送支付成功通知异常: orderNo={}", order.getOrderNo(), e);
        }
        
        log.info("======================================");
    }
    
    @Override
    public void sendOrderStatusChangeNotification(Order order, Integer oldStatus, Integer newStatus) {
        log.info("========== 发送订单状态变更通知 ==========");
        log.info("订单号: {}", order.getOrderNo());
        log.info("状态变更: {} -> {}", getStatusText(oldStatus), getStatusText(newStatus));
        
        try {
            // 获取用户信息
            User user = userMapper.selectOneById(order.getUserId());
            if (user == null || user.getPhone() == null) {
                log.warn("用户不存在或手机号为空，无法发送短信，userId={}", order.getUserId());
                return;
            }
            
            // 只有特定状态变更才发送短信通知
            if (shouldNotifyStatusChange(oldStatus, newStatus)) {
                String statusText = getStatusText(newStatus);
                boolean success = smsService.sendOrderStatusChangeNotification(
                    user.getPhone(),
                    order.getOrderNo(),
                    statusText
                );
                
                if (success) {
                    log.info("订单状态变更通知发送成功，phone={}", maskPhone(user.getPhone()));
                } else {
                    log.error("订单状态变更通知发送失败，phone={}", maskPhone(user.getPhone()));
                }
            } else {
                log.info("当前状态变更不需要发送短信通知");
            }
            
        } catch (Exception e) {
            log.error("发送订单状态变更通知异常: orderNo={}", order.getOrderNo(), e);
        }
        
        log.info("==========================================");
    }
    
    /**
     * 判断是否需要发送状态变更通知
     */
    private boolean shouldNotifyStatusChange(Integer oldStatus, Integer newStatus) {
        // 订单状态: 1-待支付, 2-待接单, 3-制作中, 4-待取餐, 5-已完成, 6-已取消
        // 以下状态变更需要通知:
        // 待接单 -> 制作中(商家已接单，开始制作)
        // 制作中 -> 待取餐(订单已完成，可以取餐)
        // 任何状态 -> 已取餐(订单被取走)
        
        if (newStatus == 6) {
            // 订单被取走，总是通知
            return true;
        }
        
        if (oldStatus == 2 && newStatus == 3) {
            // 商家已接单，开始制作
            return true;
        }
        
        if (oldStatus == 3 && newStatus == 4) {
            // 订单已完成，可以取餐
            return true;
        }
        
        return false;
    }
    
    /**
     * 获取订单状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        
        switch (status) {
            case 1:
                return "待支付";
            case 2:
                return "待接单";
            case 3:
                return "制作中";
            case 4:
                return "待取餐";
            case 5:
                return "已完成";
            case 6:
                return "已取消";
            default:
                return "未知状态";
        }
    }
    
    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
