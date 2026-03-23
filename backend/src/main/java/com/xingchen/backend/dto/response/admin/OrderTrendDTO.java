package com.xingchen.backend.dto.response.admin;

import com.xingchen.backend.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 订单趋势 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrendDTO {
    private List<Order> orders;
    private Integer days;
}
