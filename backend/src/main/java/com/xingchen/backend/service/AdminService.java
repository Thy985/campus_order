package com.xingchen.backend.service;

import com.xingchen.backend.entity.Order;
import com.xingchen.backend.entity.User;
import com.xingchen.backend.dto.response.admin.*;

import java.util.List;

/**
 * 管理员服务接口 */
public interface AdminService {

    /**
     * 获取用户列表
     */
    UserListResponse getUserList(Integer page, Integer pageSize, String keyword, Integer status);

    /**
     * 修改用户状态
     */
    boolean updateUserStatus(Long userId, Integer status);

    /**
     * 获取用户详情
     */
    User getUserDetail(Long userId);

    /**
     * 获取订单列表
     */
    OrderListResponseDTO getOrderList(Integer page, Integer pageSize, Integer status,
                                     Long merchantId, Long userId);

    /**
     * 获取订单统计
     */
    OrderStatisticsDTO getOrderStatistics();

    /**
     * 取消订单
     */
    boolean cancelOrder(Long orderId, String reason);

    /**
     * 获取订单详情
     */
    OrderDetailResponseDTO getOrderDetail(Long orderId);

    /**
     * 删除用户（软删除）
     */
    boolean deleteUser(Long userId);

    /**
     * 获取仪表盘概览
     */
    DashboardOverviewDTO getDashboardOverview();

    /**
     * 获取订单趋势
     */
    OrderTrendDTO getOrderTrend(Integer days);

    /**
     * 获取最近订单
     */
    RecentOrdersDTO getRecentOrders(Integer limit);
}
