package com.xingchen.backend.service;

/**
 * 订单超时处理服务接口
 * 定时扫描并处理超时订单
 */
public interface OrderTimeoutService {

    /**
     * 定时扫描超时订单
     */
    void scanTimeoutOrders();

    /**
     * 手动触发超时检查（用于测试）
     */
    void manualCheckTimeout();
}
