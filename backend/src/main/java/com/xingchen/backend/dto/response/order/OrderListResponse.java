package com.xingchen.backend.dto.response.order;

import com.xingchen.backend.entity.Order;
import com.xingchen.backend.entity.OrderItem;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OrderListResponse {

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 订单列表
     */
    private List<Order> orderList;

    /**
     * 页码
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 订单明细映射（key: orderId, value: 订单明细列表）
     */
    private Map<Long, List<OrderItem>> orderItemsMap;

    /**
     * 设置订单列表（兼容方法）
     */
    public void setOrders(List<Order> orders) {
        this.orderList = orders;
    }

    /**
     * 获取订单列表（兼容方法）
     */
    public List<Order> getOrders() {
        return this.orderList;
    }
}
