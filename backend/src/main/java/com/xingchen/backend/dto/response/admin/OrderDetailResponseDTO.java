package com.xingchen.backend.dto.response.admin;

import com.xingchen.backend.entity.Order;
import com.xingchen.backend.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 订单详情响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponseDTO {
    private Order order;
    private List<OrderItem> orderItems;
}
