package com.xingchen.backend.service.impl;

import com.xingchen.backend.entity.Order;
import com.xingchen.backend.service.OrderStatusService;
import com.xingchen.backend.util.constant.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * 订单状态机服务实现类
 */
@Slf4j
@Service
public class OrderStatusServiceImpl implements OrderStatusService {

    @Override
    public boolean canTransition(Integer currentStatus, Integer targetStatus) {
        int[] allowedTransitions = getAllowedTransitions(currentStatus);
        boolean canTransition = Arrays.stream(allowedTransitions)
                .anyMatch(status -> status == targetStatus);

        if (!canTransition) {
            log.warn("状态转换不合法: {} -> {}",
                    getStatusName(currentStatus), getStatusName(targetStatus));
        }

        return canTransition;
    }

    @Override
    public void transition(Order order, Integer targetStatus, Long operatorId,
                              Integer operatorType, String remark) {
        Integer currentStatus = order.getStatus();

        // 检查转换是否合法
        if (!canTransition(currentStatus, targetStatus)) {
            throw new IllegalStateException(
                    String.format("订单状态转换不合法: %s -> %s",
                            getStatusName(currentStatus), getStatusName(targetStatus))
            );
        }

        // 执行状态转换
        order.setStatus(targetStatus);

        // 根据目标状态设置相应的时间字段
        switch (targetStatus) {
            case Constants.OrderStatus.WAIT_ACCEPT:
                order.setPayStatus(Constants.PayStatus.PAID);
                break;
            case Constants.OrderStatus.MAKING:
                order.setAcceptTime(java.time.LocalDateTime.now());
                break;
            case Constants.OrderStatus.COMPLETED:
                order.setFinishTime(java.time.LocalDateTime.now());
                break;
            case Constants.OrderStatus.CANCELLED:
            case Constants.OrderStatus.REFUSED:
                order.setCancelTime(java.time.LocalDateTime.now());
                order.setCancelReason(remark);
                break;
        }

        log.info("订单状态转换成功: orderNo={}, {} -> {}, operator={}",
                order.getOrderNo(),
                getStatusName(currentStatus),
                getStatusName(targetStatus),
                operatorId);
    }

    /**
     * 获取允许的状态转换
     */
    private int[] getAllowedTransitions(int currentStatus) {
        switch (currentStatus) {
            case Constants.OrderStatus.WAIT_PAY:
                return Constants.OrderStatusTransition.FROM_WAIT_PAY;
            case Constants.OrderStatus.WAIT_ACCEPT:
                return Constants.OrderStatusTransition.FROM_WAIT_ACCEPT;
            case Constants.OrderStatus.MAKING:
                return Constants.OrderStatusTransition.FROM_MAKING;
            case Constants.OrderStatus.WAIT_PICKUP:
                return Constants.OrderStatusTransition.FROM_WAIT_PICKUP;
            case Constants.OrderStatus.COMPLETED:
                return Constants.OrderStatusTransition.FROM_COMPLETED;
            case Constants.OrderStatus.CANCELLED:
                return Constants.OrderStatusTransition.FROM_CANCELLED;
            case Constants.OrderStatus.REFUSED:
                return Constants.OrderStatusTransition.FROM_REFUSED;
            default:
                return new int[0];
        }
    }

    @Override
    public String getStatusName(Integer status) {
        if (status == null) return "未知状态";
        switch (status) {
            case Constants.OrderStatus.WAIT_PAY:
                return "待支付";
            case Constants.OrderStatus.WAIT_ACCEPT:
                return "待接单";
            case Constants.OrderStatus.MAKING:
                return "制作中";
            case Constants.OrderStatus.WAIT_PICKUP:
                return "待取餐";
            case Constants.OrderStatus.COMPLETED:
                return "已完成";
            case Constants.OrderStatus.CANCELLED:
                return "已取消";
            case Constants.OrderStatus.REFUSED:
                return "已拒绝";
            default:
                return "未知状态";
        }
    }

    @Override
    public boolean isCompleted(Integer status) {
        return status == Constants.OrderStatus.COMPLETED
                || status == Constants.OrderStatus.CANCELLED
                || status == Constants.OrderStatus.REFUSED;
    }
}
