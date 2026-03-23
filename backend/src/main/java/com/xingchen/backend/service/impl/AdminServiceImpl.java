package com.xingchen.backend.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.xingchen.backend.entity.Order;
import com.xingchen.backend.entity.OrderItem;
import com.xingchen.backend.entity.User;
import com.xingchen.backend.mapper.OrderItemMapper;
import com.xingchen.backend.mapper.OrderMapper;
import com.xingchen.backend.mapper.UserMapper;
import com.xingchen.backend.mapper.MerchantMapper;
import com.xingchen.backend.mapper.ProductMapper;
import com.xingchen.backend.service.AdminService;
import com.xingchen.backend.dto.response.admin.*;
import com.xingchen.backend.util.constant.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员服务实现类
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final MerchantMapper merchantMapper;
    private final ProductMapper productMapper;

    @Override
    public UserListResponse getUserList(Integer page, Integer pageSize, String keyword, Integer status) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .from(User.class)
                .where("is_deleted = 0");

        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and("phone LIKE ? OR nickname LIKE ?", "%" + keyword + "%", "%" + keyword + "%");
        }

        if (status != null) {
            queryWrapper.and("status = ?", status);
        }

        // 查询总数
        long total = userMapper.selectCountByQuery(queryWrapper);

        // 查询列表
        queryWrapper.orderBy("create_time DESC")
                .limit((page - 1) * pageSize, pageSize);
        List<User> users = userMapper.selectListByQuery(queryWrapper);

        return new UserListResponse(total, page, pageSize, users);
    }

    @Override
    public boolean updateUserStatus(Long userId, Integer status) {
        User user = new User();
        user.setId(userId);
        user.setStatus(status);

        int rows = userMapper.update(user);
        return rows > 0;
    }

    @Override
    public User getUserDetail(Long userId) {
        User user = userMapper.selectOneById(userId);
        if (user != null && user.getIsDeleted() == 1) {
            return null;
        }
        return user;
    }

    @Override
    public OrderListResponseDTO getOrderList(Integer page, Integer pageSize, Integer status,
                                            Long merchantId, Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .from(Order.class)
                .where("is_deleted = 0");

        if (status != null) {
            queryWrapper.and("status = ?", status);
        }

        if (merchantId != null) {
            queryWrapper.and("merchant_id = ?", merchantId);
        }

        if (userId != null) {
            queryWrapper.and("user_id = ?", userId);
        }

        // 查询总数
        long total = orderMapper.selectCountByQuery(queryWrapper);

        // 查询列表
        queryWrapper.orderBy("create_time DESC")
                .limit((page - 1) * pageSize, pageSize);
        List<Order> orders = orderMapper.selectListByQuery(queryWrapper);

        return new OrderListResponseDTO(total, page, pageSize, orders);
    }

    @Override
    public OrderStatisticsDTO getOrderStatistics() {
        // 总订单数
        QueryWrapper totalQuery = QueryWrapper.create()
                .from(Order.class)
                .where("is_deleted = 0");
        long totalOrders = orderMapper.selectCountByQuery(totalQuery);

        // 今日订单数
        QueryWrapper todayQuery = QueryWrapper.create()
                .from(Order.class)
                .where("is_deleted = 0")
                .and("DATE(create_time) = CURDATE()");
        long todayOrders = orderMapper.selectCountByQuery(todayQuery);

        // 待处理订单数
        QueryWrapper pendingQuery = QueryWrapper.create()
                .from(Order.class)
                .where("is_deleted = 0")
                .and("status IN (2, 3)"); // 待接单+制作中
        long pendingOrders = orderMapper.selectCountByQuery(pendingQuery);

        // 总营收
        QueryWrapper revenueQuery = QueryWrapper.create()
                .from(Order.class)
                .where("is_deleted = 0")
                .and("status NOT IN (6, 7)"); // 排除已取消和已拒绝
        List<Order> orders = orderMapper.selectListByQuery(revenueQuery);
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Order order : orders) {
            if (order.getTotalAmount() != null) {
                totalRevenue = totalRevenue.add(order.getTotalAmount());
            }
        }

        return new OrderStatisticsDTO(totalOrders, todayOrders, pendingOrders, totalRevenue);
    }

    @Override
    public boolean cancelOrder(Long orderId, String reason) {
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(6); // 已取?        order.setRemark(reason);

        int rows = orderMapper.update(order);
        return rows > 0;
    }

    @Override
    public OrderDetailResponseDTO getOrderDetail(Long orderId) {
        Order order = orderMapper.selectOneById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            return null;
        }

        List<OrderItem> orderItems = orderItemMapper.selectByOrderId(orderId);
        return new OrderDetailResponseDTO(order, orderItems);
    }

    @Override
    public boolean deleteUser(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setIsDeleted(Constants.DeleteFlag.DELETED);

        int rows = userMapper.update(user);
        return rows > 0;
    }

    @Override
    public DashboardOverviewDTO getDashboardOverview() {
        long totalUsers = userMapper.selectCountByQuery(
                QueryWrapper.create().where("is_deleted = ?", Constants.DeleteFlag.NOT_DELETED)
        );

        long totalMerchants = merchantMapper.selectCountByQuery(
                QueryWrapper.create().where("is_deleted = ?", Constants.DeleteFlag.NOT_DELETED)
        );

        long totalProducts = productMapper.selectCountByQuery(
                QueryWrapper.create().where("is_deleted = ?", Constants.DeleteFlag.NOT_DELETED)
        );

        long totalOrders = orderMapper.selectCountByQuery(
                QueryWrapper.create().where("is_deleted = ?", Constants.DeleteFlag.NOT_DELETED)
        );

        long todayOrders = orderMapper.selectCountByQuery(
                QueryWrapper.create()
                        .where("is_deleted = ?", Constants.DeleteFlag.NOT_DELETED)
                        .and("DATE(create_time) = CURDATE()")
        );

        long pendingOrders = orderMapper.selectCountByQuery(
                QueryWrapper.create()
                        .where("is_deleted = ?", Constants.DeleteFlag.NOT_DELETED)
                        .and("status IN (?, ?)", Constants.OrderStatus.WAIT_ACCEPT, Constants.OrderStatus.MAKING)
        );

        // 总营收
        QueryWrapper revenueQuery = QueryWrapper.create()
                .from(Order.class)
                .where("is_deleted = ?", Constants.DeleteFlag.NOT_DELETED)
                .and("status NOT IN (?, ?)", Constants.OrderStatus.CANCELLED, Constants.OrderStatus.REFUSED);
        List<Order> orders = orderMapper.selectListByQuery(revenueQuery);
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Order order : orders) {
            if (order.getTotalAmount() != null) {
                totalRevenue = totalRevenue.add(order.getTotalAmount());
            }
        }

        return new DashboardOverviewDTO(totalUsers, totalMerchants, totalProducts,
                totalOrders, todayOrders, pendingOrders, totalRevenue);
    }

    @Override
    public OrderTrendDTO getOrderTrend(Integer days) {
        // 查询最近N天的订单趋势
        QueryWrapper queryWrapper = QueryWrapper.create()
                .from(Order.class)
                .where("is_deleted = ?", Constants.DeleteFlag.NOT_DELETED)
                .and("create_time >= DATE_SUB(NOW(), INTERVAL ? DAY)", days)
                .orderBy("create_time ASC");
        List<Order> orders = orderMapper.selectListByQuery(queryWrapper);
        return new OrderTrendDTO(orders, days);
    }

    @Override
    public RecentOrdersDTO getRecentOrders(Integer limit) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .from(Order.class)
                .where("is_deleted = ?", Constants.DeleteFlag.NOT_DELETED)
                .orderBy("create_time DESC")
                .limit(0, limit);
        List<Order> orders = orderMapper.selectListByQuery(queryWrapper);
        return new RecentOrdersDTO(orders);
    }
}
