package com.xingchen.backend.dto.response.admin;

import com.xingchen.backend.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 订单列表响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderListResponseDTO {
    private Long total;
    private Integer page;
    private Integer pageSize;
    private List<Order> orderList;
}
