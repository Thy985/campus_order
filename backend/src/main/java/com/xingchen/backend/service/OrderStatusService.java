package com.xingchen.backend.service;

import com.xingchen.backend.entity.Order;

/**
 * 订单状态管理服务
 * 管理订单状态的流转和校验
 */
public interface OrderStatusService {
    
    /**
     * 检查状态转换是否合法
     * @param currentStatus 当前状态
     * @param targetStatus  目标状态
     * @return 是否允许转换
     */
    boolean canTransition(Integer currentStatus, Integer targetStatus);
    
    /**
     * 执行状态转换
     * @param order 订单对象
     * @param targetStatus 目标状态
     * @param operatorId   操作人ID
     * @param operatorType 操作人类型（1-用户 2-商家 3-系统）
     * @param remark       操作备注
     */
    void transition(Order order, Integer targetStatus, Long operatorId, Integer operatorType, String remark);
    
    /**
     * 获取状态名称
     */
    String getStatusName(Integer status);
    
    /**
     * 判断订单是否已完成
     */
    boolean isCompleted(Integer status);
}
