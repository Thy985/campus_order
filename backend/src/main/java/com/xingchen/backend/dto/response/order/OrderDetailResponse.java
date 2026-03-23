package com.xingchen.backend.dto.response.order;

import com.xingchen.backend.entity.Order;
import com.xingchen.backend.entity.OrderItem;
import lombok.Data;

import java.util.List;

@Data
public class OrderDetailResponse {
    
    /**
     * 订单信息
     */
    private Order order;
    
    /**
     * 订单明细列表
     */
    private List<OrderItem> orderItems;
}
