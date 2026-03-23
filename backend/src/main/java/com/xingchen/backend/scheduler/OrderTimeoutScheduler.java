package com.xingchen.backend.scheduler;

import com.xingchen.backend.entity.Order;
import com.xingchen.backend.entity.Payment;
import com.xingchen.backend.mapper.OrderMapper;
import com.xingchen.backend.mapper.PaymentMapper;
import com.xingchen.backend.service.OrderService;
import com.xingchen.backend.service.payment.AlipayService;
import com.xingchen.backend.util.constant.Constants;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单超时定时任务
 *
 * 功能说明：
 * 1. 自动检测超时未支付订单
 * 2. 自动取消超时订单
 * 3. 回滚库存
 * 4. 关闭支付
 *
 * @author 系统
 * @date 2026-02-13
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutScheduler {

    private final OrderMapper orderMapper;
    private final PaymentMapper paymentMapper;
    private final OrderService orderService;
    private final com.xingchen.backend.service.notification.NotificationService notificationService;

    private final AlipayService alipayService;

    /**
     * 订单超时时间（分钟），从配置文件读取
     */
    @Value("${order.timeout:15}")
    private Integer orderTimeout;
    
    /**
     * 定时任务：检测并取消超时订单
     * 
     * 执行频率：每5分钟执行一次
     * Cron表达式：0 0/5 * * * ?
     * 说明：每小时的第0、5、10、15...分钟执行
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void cancelTimeoutOrders() {
        log.info("========== 开始执行订单超时检测任务 ==========");
        
        long startTime = System.currentTimeMillis();
        int cancelCount = 0;
        
        try {
            // 1. 查询超时未支付订单
            List<Order> timeoutOrders = findTimeoutOrders();
            
            if (timeoutOrders.isEmpty()) {
                log.info("未发现超时订单");
                return;
            }
            
            log.info("发现{}个超时订单，开始处理", timeoutOrders.size());
            
            // 2. 逐个处理超时订单
            for (Order order : timeoutOrders) {
                try {
                    cancelTimeoutOrder(order);
                    cancelCount++;
                } catch (Exception e) {
                    log.error("取消超时订单失败: orderId={}, orderNo={}", 
                            order.getId(), order.getOrderNo(), e);
                }
            }
            
            long endTime = System.currentTimeMillis();
            log.info("订单超时检测任务完成：处理{}个订单，成功取消{}个，耗时{}ms", 
                    timeoutOrders.size(), cancelCount, (endTime - startTime));
            
        } catch (Exception e) {
            log.error("订单超时检测任务执行异常", e);
        } finally {
            log.info("========== 订单超时检测任务结束 ==========");
        }
    }
    
    /**
     * 查询超时未支付订单
     * 
     * 查询条件：
     * 1. 订单状态为待支付（status = 1）
     * 2. 创建时间超过超时时间
     * 3. 未被删除（is_deleted = 0）
     * 
     * @return 超时订单列表
     */
    private List<Order> findTimeoutOrders() {
        // 计算超时时间点
        LocalDateTime timeoutDate = LocalDateTime.now().minusMinutes(orderTimeout);

        log.debug("查询超时订单：超时时间点={}, 超时阈值{}分钟", timeoutDate, orderTimeout);

        // 构建查询条件
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("status = ?", Constants.OrderStatus.WAIT_PAY)  // 待支付状态
                .and("create_time < ?", timeoutDate)  // 创建时间早于超时时间点
                .and("is_deleted = ?", Constants.DeleteFlag.NOT_DELETED);  // 未删除
        
        List<Order> orders = orderMapper.selectListByQuery(queryWrapper);
        
        log.debug("查询到{}个超时订单", orders.size());
        
        return orders;
    }
    
    /**
     * 取消超时订单
     * 
     * 处理步骤：
     * 1. 更新订单状态为已取消
     * 2. 回滚库存
     * 3. 关闭支付订单（如果已创建支付）
     * 4. 记录取消原因
     * 
     * @param order 超时订单
     */
    private void cancelTimeoutOrder(Order order) {
        log.info("开始取消超时订单: orderId={}, orderNo={}, createTime={}", 
                order.getId(), order.getOrderNo(), order.getCreateTime());
        
        try {
            // 1. 更新订单状态为已取消
            order.setStatus(Constants.OrderStatus.CANCELLED);
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.update(order);
            
            log.info("订单状态已更新为已取消: orderId={}", order.getId());
            
            // 2. 回滚库存
            rollbackStock(order);
            
            // 3. 关闭支付订单
            closePayment(order);
            
            // 4. 发送通知
            sendNotification(order);
            
            log.info("超时订单取消成功: orderId={}, orderNo={}", order.getId(), order.getOrderNo());
            
        } catch (Exception e) {
            log.error("取消超时订单失败: orderId={}", order.getId(), e);
            throw e;
        }
    }
    
    /**
     * 回滚库存
     * 
     * 说明：
     * 订单取消后，需要将扣减的库存归还
     * 
     * @param order 订单
     */
    private void rollbackStock(Order order) {
        try {
            // 调用订单服务的库存回滚方法
            orderService.rollbackStock(order.getId());
            
            log.info("库存回滚成功: orderId={}", order.getId());
        } catch (Exception e) {
            log.error("库存回滚失败: orderId={}", order.getId(), e);
            // 库存回滚失败不影响订单取消，记录日志即可
        }
    }
    
    /**
     * 关闭支付订单
     * 
     * 说明：
     * 如果订单已创建支付，需要调用支付平台关闭订单
     * 
     * @param order 订单
     */
    private void closePayment(Order order) {
        try {
            // 查询支付记录
            Payment payment = paymentMapper.selectByOrderId(order.getId());
            
            if (payment == null) {
                log.debug("订单未创建支付记录，无需关闭: orderId={}", order.getId());
                return;
            }
            
            // 如果支付已完成，不需要关闭
            if (payment.getStatus() == Constants.PaymentStatus.PAID) {
                log.warn("订单已支付，不应取消: orderId={}, paymentNo={}", 
                        order.getId(), payment.getPaymentNo());
                return;
            }
            
            // 如果是支付宝支付，支付宝侧无需关闭（沙箱环境会自动过期）
            // 更新本地支付状态为关闭
            if (payment.getChannel() == Constants.PaymentMethod.ALIPAY) {
                log.debug("支付宝支付订单无需主动关闭，沙箱环境会自动处理: paymentNo={}", payment.getPaymentNo());
            }
            
            // 更新支付状态为失败
            paymentMapper.closePayment(payment.getId(), Constants.PaymentStatus.FAILED);
            
            log.info("支付记录状态已更新为失败: paymentNo={}", payment.getPaymentNo());
            
        } catch (Exception e) {
            log.error("关闭支付订单失败: orderId={}", order.getId(), e);
            // 关闭支付失败不影响订单取消，记录日志即可
        }
    }
    
    /**
     * 发送通知
     * 
     * 说明：
     * 订单取消后，通知用户
     * 
     * @param order 订单
     */
    private void sendNotification(Order order) {
        try {
            // 发送订单超时取消通知
            notificationService.sendOrderTimeoutNotification(order);
            
            log.info("超时取消通知发送成功: orderId={}", order.getId());
        } catch (Exception e) {
            log.error("超时取消通知发送失败: orderId={}", order.getId(), e);
            // 通知发送失败不影响订单取消，记录日志即可
        }
    }
    
    /**
     * 手动触发超时检测（用于测试）
     * 
     * 注意：生产环境应删除此方法或添加权限控制
     */
    public void triggerManually() {
        log.info("手动触发订单超时检测任务");
        cancelTimeoutOrders();
    }
}
